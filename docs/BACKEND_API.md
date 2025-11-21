# CloudCare Backend API Documentation

**Version:** 1.0  
**Last Updated:** November 2025  
**Base URL:** `https://cloudcare.pipfactor.com/api/v1`

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
- [Database Architecture](#database-architecture)
- [Setup & Deployment](#setup--deployment)

---

## Overview

CloudCare backend is built with FastAPI (Python) and uses a dual-database architecture:
- **PostgreSQL**: User auth, patient/doctor/hospital profiles, consents, device pairings
- **MongoDB**: Health metrics (30K+ individual readings), medical documents (GridFS)

**Key Features:**
- Aadhar-based universal patient identification (HMAC-SHA256)
- JWT authentication with role-based access control (RBAC)
- Real-time health metrics from wearable devices
- Apple Health/HealthKit integration
- Cross-facility document sharing
- Patient-controlled consent system

---

## Architecture

### System Architecture

```
Android App (Kotlin/Compose)
        ↓ HTTPS/REST
FastAPI Backend (Python)
        ↓
   ├─ PostgreSQL (Structured Data)
   │   ├─ Users & Authentication
   │   ├─ Patients (Aadhar UID)
   │   ├─ Doctors
   │   ├─ Hospitals
   │   ├─ Consents
   │   └─ Device Pairings
   │
   └─ MongoDB (Health Data)
       ├─ health_metrics (30K+ documents)
       └─ Medical Documents (GridFS)
```

### Database Rationale

| Data Type | Database | Reason |
|-----------|----------|--------|
| User accounts, auth | PostgreSQL | ACID transactions, strong consistency |
| Patient/Doctor profiles | PostgreSQL | Relational integrity (facilities, consents) |
| Device pairings | PostgreSQL | Foreign key constraints |
| Health metrics | MongoDB | High write throughput, time-series optimization |
| Medical documents | MongoDB | GridFS for large file storage |

---

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | FastAPI | 3.0.0 |
| Language | Python | 3.11+ |
| ORM | Prisma | Latest |
| PostgreSQL | PostgreSQL | 15+ |
| MongoDB | MongoDB | 6.0+ |
| Cache | Redis | 7+ |
| Deployment | Docker Compose | Latest |
| Tunnel | Cloudflare | - |

---

## Authentication

### JWT Token System

**Flow:**
1. User logs in with email + password
2. Backend returns `access_token` (30 min) and `refresh_token` (7 days)
3. Client includes token in header: `Authorization: Bearer <token>`
4. Token includes user role (PATIENT, DOCTOR, HOSPITAL_ADMIN)

**Token Structure:**
```json
{
  "sub": "user_id",
  "role": "PATIENT",
  "exp": 1234567890
}
```

### Role-Based Access Control

| Role | Access |
|------|--------|
| **PATIENT** | Own profile, own health data, own consents, linked facilities |
| **DOCTOR** | Assigned patients (with consent), patient health data, appointments |
| **HOSPITAL_ADMIN** | Hospital profile, staff, resources, admitted patients |
| **SYSTEM_ADMIN** | Full access (logs only, no patient data modification) |

---

## API Endpoints

**Complete Endpoint List:**
- **Authentication:** 6 endpoints
- **Wearables & Health:** 15 endpoints
- **Consent Management:** 5 endpoints
- **Doctor:** 3 endpoints
- **Patient:** 3 endpoints
- **Medical Documents:** 3 endpoints
- **Hospital:** 8 endpoints
- **Health Check:** 1 endpoint

**Total:** 44 API endpoints

---

### Authentication Endpoints

#### 1. Patient Signup
```http
POST /auth/signup/patient
Content-Type: application/json

{
  "email": "patient@example.com",
  "password": "SecurePass123!",
  "aadhar_number": "123456789012",
  "first_name": "John",
  "last_name": "Doe",
  "date_of_birth": "1990-01-15",
  "gender": "MALE",
  "blood_group": "O_POSITIVE",
  "phone_primary": "+919876543210",
  "emergency_contact_name": "Jane Doe",
  "emergency_contact_phone": "+919876543211",
  "emergency_contact_relation": "Spouse",
  "address_line1": "123 Main St",
  "city": "Mumbai",
  "state": "Maharashtra",
  "postal_code": "400001",
  "country": "India"
}
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "bearer",
  "user": {
    "id": "uuid",
    "email": "patient@example.com",
    "role": "PATIENT",
    "patient_id": "uuid",
    "aadhar_uid": "a3f5e8d9c2b1..." // HMAC-SHA256 hash
  }
}
```

#### 2. Doctor Signup
```http
POST /auth/signup/doctor
Content-Type: application/json

{
  "email": "doctor@example.com",
  "password": "SecurePass123!",
  "medical_license_no": "MCI-12345-2015",
  "first_name": "Dr. Sarah",
  "last_name": "Smith",
  "specialization": "Cardiology",
  "phone_primary": "+919123456789",
  "hospital_code": "HC-AB12CD", // Links to hospital (6 chars after HC-)
  "qualifications": ["MBBS", "MD", "DM"],
  "experience_years": 10,
  "consultation_fee": 1500.00,
  "address_line1": "Hospital Campus",
  "city": "Delhi",
  "state": "Delhi",
  "postal_code": "110001",
  "country": "India"
}
```

#### 3. Hospital Signup
```http
POST /auth/signup/hospital
Content-Type: application/json

{
  "email": "admin@hospital.com",
  "password": "SecurePass123!",
  "name": "City General Hospital",
  "phone_primary": "+911234567890",
  "phone_emergency": "+911234567899",
  "address_line1": "Hospital Road",
  "city": "Bangalore",
  "state": "Karnataka",
  "postal_code": "560001",
  "country": "India",
  "facility_type": "MULTI_SPECIALTY_HOSPITAL",
  "total_beds": 500,
  "icu_beds": 50,
  "emergency_beds": 100,
  "operation_theatres": 10,
  "has_emergency": true,
  "has_ambulance": true,
  "has_pharmacy": true,
  "has_lab": true,
  "has_blood_bank": true
}
```

**Response:** Auto-generates unique `hospital_code` (e.g., `HC-AB12CD` - format: HC-XXXXXX with 6 alphanumeric chars)

#### 4. Login (All Roles)
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response:** Same as signup

#### 5. Refresh Token
```http
POST /auth/refresh
Content-Type: application/json

{
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 6. Get Current User
```http
GET /auth/me
Authorization: Bearer <token>
```

---

### Wearables & Health Metrics

#### 1. Get Comprehensive Metrics (Single Call)
```http
GET /wearables/metrics/comprehensive?patient_id={patient_id}&days=30
Authorization: Bearer <token>
```

**Response:**
```json
{
  "patient_id": "uuid",
  "request_timestamp": "2025-11-19T09:34:26Z",
  "summary": {
    "steps": {"total": 8542, "change": "-78%"},
    "calories": {"total": 2150, "change": "-71%"},
    "heart_rate": {"avg": 79.5, "min": 58.0, "max": 152.0, "change": "-5%"},
    "distance": {"total": 5.2, "unit": "km"},
    "flights_climbed": {"total": 12},
    "sleep": {
      "time_in_bed": 7.08,
      "time_asleep": 4.65,
      "unit": "hours",
      "stages": {"awake": 0.4, "rem": 0.62, "core": 3.41, "deep": 0.62}
    }
  },
  "time_series": {
    "steps": [
      {"date": "2025-11-19", "total": 2322, "avg": 91, "count": 115}
    ],
    "steps_hourly": [
      {"date": "2025-11-19 12:00", "total": 319, "count": 3}
    ],
    "calories": [...],
    "calories_hourly": [...],
    "heart_rate": [...],
    "heart_rate_hourly": [...],
    "distance": [...],
    "distance_hourly": [...],
    "sleep": [...]
  },
  "device_info": {
    "last_sync": "2025-11-19T09:00:13Z",
    "total_metrics": 30186
  }
}
```

**Features:**
- Single API call replaces 5+ individual endpoints
- Returns daily aggregated data (30 days)
- Returns hourly data (last 24 hours) for daily view
- 73% faster than multiple calls

#### 2. Today's Summary
```http
GET /wearables/summary/today?patient_id={patient_id}
Authorization: Bearer <token>
```

#### 3. Get Recent Metrics
```http
GET /wearables/metrics/recent?patient_id={patient_id}&limit=100
Authorization: Bearer <token>
```

#### 4. Get Aggregated Metrics
```http
GET /wearables/metrics/aggregated?patient_id={patient_id}&metric_type=heart_rate&start_date=2025-11-01&end_date=2025-11-30
Authorization: Bearer <token>
```

#### 5. Get Metrics by Type
```http
GET /wearables/metrics/by-type?patient_id={patient_id}&metric_type=steps&days=30
Authorization: Bearer <token>
```

#### 6. Get Sleep Trends
```http
GET /wearables/metrics/sleep-trends?patient_id={patient_id}&days=30
Authorization: Bearer <token>
```

#### 7. Get Heart Rate Trends
```http
GET /wearables/metrics/heart-rate-trends?patient_id={patient_id}&days=30
Authorization: Bearer <token>
```

#### 8. Register Deviceta
```http
GET /wearables/devices?patient_id={patient_id}
Authorization: Bearer <token>
```

#### 9. Register Device
```http
POST /wearables/devices
Authorization: Bearer <token>
Content-Type: application/json

{
  "patient_id": "uuid",
  "name": "Apple Watch",
  "type": "apple_watch",
  "device_id": "external-device-id"
}
```

#### 11. Apple Health Import (Single File)
```http
POST /wearables/sync
Authorization: Bearer <token>
Content-Type: application/json

{
  "patient_id": "uuid",
  "device_id": "uuid",
  "metrics": [
    {
      "metric_type": "heart_rate",
      "value": 72.5,
      "unit": "bpm",
      "timestamp": "2025-11-19T10:30:00Z"
    }
  ]
}
```

#### 12. Apple Health Batch Import
```http
POST /wearables/import/apple-health
Authorization: Bearer <token>
Content-Type: application/json

{
  "deviceId": "ios-device-id",
  "userId": "user-id",
  "exportTimestamp": "2025-11-13T06:49:53Z",
  "metrics": [
    {
      "type": "HKQuantityTypeIdentifierHeartRate",
      "value": 79,
      "unit": "bpm",
      "startDate": "2025-11-12T06:52:56Z",
      "endDate": "2025-11-12T06:52:56Z",
      "sourceApp": "com.apple.health",
      "metadata": {"device": "Apple Watch"}
    }
  ]
}
```

#### 13. Device Pairing (QR Code)
```http
POST /wearables/import/apple-health/batch
Authorization: Bearer <token>
Content-Type: application/json

[
  { /* export 1 */ },
  { /* export 2 */ }
]
```

**Supported Metrics:**
- `HKQuantityTypeIdentifierHeartRate` → heart_rate
- `HKQuantityTypeIdentifierStepCount` → steps
- `HKQuantityTypeIdentifierActiveEnergyBurned` → calories
- `HKQuantityTypeIdentifierDistanceWalkingRunning` → distance
- `HKQuantityTypeIdentifierFlightsClimbed` → flights_climbed
- `HKQuantityTypeIdentifierRestingHeartRate` → resting_heart_rate
- `HKQuantityTypeIdentifierVO2Max` → vo2_max
- Sleep analysis metrics

#### 14. Get Paired Devices
```http
POST /wearables/devices/pair
Authorization: Bearer <token>
Content-Type: application/json

{
  "ios_user_id": "uuid",
  "ios_device_id": "device-id",
  "android_user_id": "uuid",
  "device_name": "Apple Watch Series 9",
  "device_type": "apple_watch",
  "pairing_code": "qr-code-data"
}
```

#### 15. Unpair Device
```http
GET /wearables/devices/paired?patient_id={patient_id}
Authorization: Bearer <token>
```

**Total Wearables Endpoints:** 15

---
```http
DELETE /wearables/devices/unpair/{pairing_id}
Authorization: Bearer <token>
```

---

### Consent Management

#### 1. Request Consent (Doctor scans patient QR)
```http
POST /consents/request
Authorization: Bearer <token>
Content-Type: application/json

{
  "patient_id": "uuid",
  "doctor_id": "uuid",
  "facility_name": "City Hospital",
  "request_type": "full_access",
  "description": "Request access for cardiac consultation"
}
```

**Response:**
```json
{
  "id": "consent-uuid",
  "patient_id": "uuid",
  "facility_name": "City Hospital",
  "status": "PENDING",
  "requested_at": "2025-11-20T10:00:00Z"
}
```

#### 2. Get Patient Consents
```http
GET /consents/patient/{patient_id}?status_filter=PENDING
Authorization: Bearer <token>
```

**Query Parameters:**
- `status_filter`: PENDING, APPROVED, DENIED, REVOKED (optional)

#### 3. Update Consent Status
```http
PATCH /consents/{consent_id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "APPROVED"  // or DENIED, REVOKED
}
```

**Effect:**
- `APPROVED`: Doctor gets full access (patient status → ACTIVE)
- `DENIED`: Doctor remains locked out
- `REVOKED`: Doctor loses access (patient status → LOCKED)

#### 4. Delete Consent
```http
DELETE /consents/{consent_id}
Authorization: Bearer <token>
```

**Note:** Only for DENIED or expired consents

#### 5. Cleanup All Consents (Testing)
```http
DELETE /consents/cleanup/all
Authorization: Bearer <token>
```

**Warning:** Deletes all consents, resets all doctor-patient relationships to LOCKED

---

### Doctor Endpoints

#### 1. Get Doctor Profile
```http
GET /doctors/{doctor_id}/profile
Authorization: Bearer <token>
```

#### 2. Get Assigned Patients
```http
GET /doctors/{doctor_id}/patients
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "patient_id": "uuid",
    "patient_name": "John Doe",
    "status": "ACTIVE",  // LOCKED, ACTIVE, STABLE, MONITORING, CRITICAL
    "condition": "Cardiac consultation",
    "next_appointment": "2025-11-25T10:00:00Z",
    "emergency_flag": false
  }
]
```

**Access Levels:**
- `LOCKED`: Name only, no details
- `ACTIVE/STABLE/MONITORING/CRITICAL`: Full access (age, gender, blood group, phone, medical history, wearable data)

#### 3. Remove Patient (Revoke Consent)
```http
DELETE /doctors/{doctor_id}/patients/{patient_id}
Authorization: Bearer <token>
```

**Effect:** Revokes consent, patient status → LOCKED

#### 4. Get Doctor's Hospitals
```http
GET /doctors/{doctor_id}/hospitals
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "hospital_id": "uuid",
    "hospital_name": "Apollo Hospital Bangalore",
    "hospital_code": "HC-B2631D",
    "is_primary": true,
    "joined_at": "2025-11-21T10:00:00Z"
  }
]
```

#### 5. Update Doctor's Hospitals
```http
POST /doctors/{doctor_id}/hospitals
Authorization: Bearer <token>
Content-Type: application/json

{
  "hospital_ids": ["uuid1", "uuid2", "uuid3"],
  "primary_hospital_id": "uuid1"
}
```

**Effect:** Updates doctor's hospital associations, maintains many-to-many relationship

---

### Patient Endpoints

#### 1. Get Patient Profile
```http
GET /patients/{patient_id}/profile
Authorization: Bearer <token>
```

**Response:**
```json
{
  "id": "uuid",
  "first_name": "John",
  "last_name": "Doe",
  "date_of_birth": "1990-01-15",
  "age": 35,
  "gender": "MALE",
  "blood_group": "O_POSITIVE",
  "phone_primary": "+919876543210",
  "email": "patient@example.com",
  "address_line1": "123 Main St",
  "city": "Mumbai",
  "state": "Maharashtra",
  "emergency_contact_name": "Jane Doe",
  "emergency_contact_phone": "+919876543211",
  "allergies": ["Penicillin"],
  "chronic_conditions": ["Hypertension"],
  "current_medications": ["Lisinopril 10mg"],
  "insurance_provider": "Star Health",
  "insurance_policy_no": "SH/2024/123456"
}
```

#### 2. Update Patient Profile
```http
POST /patients/{patient_id}/profile
Authorization: Bearer <token>
Content-Type: application/json

{
  "weight_kg": 75.5,
  "height_cm": 175.0,
  "allergies": ["Penicillin", "Pollen"],
  "current_medications": ["Lisinopril 10mg", "Aspirin 81mg"]
}
```

#### 3. Lookup Records (Aadhar-based)
```http
POST /patients/lookup-records
Authorization: Bearer <token>
Content-Type: application/json

{
  "aadhar_uid": "hashed-uid",
  "facility_name": "Hospital Name"
}
```

**Use Case:** Request records from hospital not yet on CloudCare

---

### Medical Documents

#### 1. Upload Document
```http
POST /documents/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

{
  "patient_id": "uuid",
  "title": "Blood Test Report",
  "record_type": "LAB_REPORT",
  "date": "2025-11-15",
  "file": <binary>
}
```

**Record Types:** LAB_REPORT, PRESCRIPTION, GENERAL, CONSULTATION, IMAGING

#### 2. Get Patient Documents
```http
GET /documents/{patient_id}
Authorization: Bearer <token>
```

#### 3. Delete Document
```http
DELETE /documents/{record_id}
Authorization: Bearer <token>
```

---

### Hospital Endpoints

#### 1. Get Hospital Dashboard
```http
GET /hospitals/{hospital_id}/dashboard
Authorization: Bearer <token>
```

**Response:**
```json
{
  "total_beds": 500,
  "occupied_beds": 320,
  "available_beds": 180,
  "icu_beds": 50,
  "emergency_cases": 12,
  "critical_patients": 5,
  "total_doctors": 80,
  "active_doctors": 65
}
```

#### 2. Get Hospital Doctors
```http
GET /hospitals/{hospital_id}/doctors
Authorization: Bearer <token>
```

#### 3. Get Admitted Patients
```http
GET /hospitals/{hospital_id}/patients
Authorization: Bearer <token>
```

#### 4. Admit Patient
```http
POST /hospitals/{hospital_id}/admit
Authorization: Bearer <token>
Content-Type: application/json

{
  "patient_id": "uuid",
  "doctor_id": "uuid",
  "condition": "Chest pain",
  "severity": "HIGH",
  "department": "Cardiology"
}
```

#### 5. Discharge Patient
```http
POST /hospitals/{hospital_id}/patients/{patient_id}/discharge
Authorization: Bearer <token>
```

#### 6. Update Resources
```http
PUT /hospitals/{hospital_id}/resources
Authorization: Bearer <token>
Content-Type: application/json

{
  "total_beds": 500,
  "available_beds": 180,
  "oxygen_cylinders": 200,
  "ventilators": 50,
  "ambulances": 10,
  "blood_bags": 500
}
```

#### 7. Search Hospitals
```http
GET /hospitals/search?query={name_or_code}
Authorization: Bearer <token>
```

**Query Parameters:**
- `query`: Hospital name or code (case-insensitive)
- Returns up to 50 matching hospitals

**Response:**
```json
[
  {
    "id": "uuid",
    "name": "Apollo Hospital Bangalore",
    "hospital_code": "HC-B2631D",
    "city": "Bangalore",
    "state": "Karnataka"
  }
]
```

#### 8. Get All Hospitals
```http
GET /hospitals/
Authorization: Bearer <token>
```

---

### Health Check Endpoint

#### 1. Health Check
```http
GET /health
```

**Response:**
```json
{
  "status": "healthy",
  "timestamp": "2025-11-20T10:00:00Z",
  "version": "1.0",
  "services": {
    "postgresql": "connected",
    "mongodb": "connected",
    "redis": "connected"
  }
}
```

**Use Case:** Monitor API availability and service health

---

## Database Architecture

### PostgreSQL Tables (20 tables)

**Core Tables:**
- `users` - Authentication (email, password, role)
- `patients` - Patient profiles (Aadhar UID, personal info, medical history)
- `doctors` - Doctor profiles (license, specialization, hospital)
- `hospitals` - Hospital profiles (auto-generated code, capacity, services)

**Relationships:**
- `doctor_patients` - Doctor-patient assignments (status: LOCKED/ACTIVE)
- `doctor_hospitals` - Doctor-hospital many-to-many (supports multiple hospitals per doctor)
- `consents` - Data access permissions (PENDING/APPROVED/DENIED/REVOKED)
- `wearable_devices` - Device registrations
- `device_pairings` - iOS-Android device links (QR code)

**Records & Activities:**
- `medical_records` - Document metadata
- `activities` - Activity feed (syncs, shares, uploads)
- `appointments` - Appointment scheduling
- `emergency_alerts` - Critical health alerts
- `emergency_cases` - Hospital emergency admissions

**Hospital Management:**
- `departments` - Hospital departments
- `hospital_resources` - Resource tracking (beds, equipment, oxygen cylinders, ventilators, ambulances, blood bags)
- `facilities` - Linked facilities (clinics, labs)
- `patient_records` - Doctor notes, prescriptions

**System:**
- `audit_logs` - All data access logged
- `document_requests` - Cross-facility record requests

### MongoDB Collections

**health_metrics Collection:**
```javascript
{
  "_id": ObjectId("..."),
  "patient_id": "uuid",
  "device_id": "uuid",
  "metric_type": "heart_rate",  // steps, calories, distance, etc.
  "value": 72.5,
  "unit": "bpm",
  "timestamp": ISODate("2025-11-19T10:30:00Z"),
  "source": "Apple Watch Series 9",
  "source_version": "watchOS 11.1",
  "metadata": {},
  "created_at": ISODate("2025-11-19T10:31:00Z")
}
```

**Indexes:**
```javascript
// Compound unique index for deduplication
db.health_metrics.createIndex(
  { "patient_id": 1, "device_id": 1, "metric_type": 1, "timestamp": 1 },
  { unique: true }
);

// Time-series queries
db.health_metrics.createIndex({ "patient_id": 1, "timestamp": -1 });
db.health_metrics.createIndex({ "metric_type": 1, "timestamp": -1 });
```

**Count:** 30,186 individual metric documents (tested with real Apple Watch data)

---

## Setup & Deployment

### Prerequisites
- Python 3.11+
- Docker & Docker Compose
- PostgreSQL 15+
- MongoDB 6.0+

### Environment Variables (.env)

```bash
# Database
DATABASE_URL="postgresql://cloudcare:password@postgres:5432/cloudcare_db"
MONGODB_URL="mongodb://mongodb:27017/cloudcare_wearables"

# JWT
JWT_SECRET_KEY="your-secret-key-here"
JWT_ALGORITHM="HS256"
ACCESS_TOKEN_EXPIRE_MINUTES=30
REFRESH_TOKEN_EXPIRE_DAYS=7

# Aadhar Security
AADHAR_HMAC_SECRET="your-hmac-secret-256-bit"
AADHAR_ENCRYPTION_KEY="your-fernet-key-here"

# Redis
REDIS_URL="redis://redis:6379"

# Cloudflare Tunnel
CLOUDFLARE_TUNNEL_TOKEN="your-tunnel-token"
```

### Docker Compose Setup

```bash
# 1. Clone repository
cd backend

# 2. Create .env file
cp .env.example .env
# Edit .env with your values

# 3. Start services
docker-compose up -d

# 4. Run migrations
docker-compose exec api prisma migrate deploy

# 5. Access at
# Local: http://localhost:8000
# Tunnel: https://cloudcare.pipfactor.com
```

### Services

| Service | Port | Purpose |
|---------|------|---------|
| FastAPI | 8000 | Main API server |
| PostgreSQL | 5432 | User data, metadata |
| MongoDB | 27017 | Health metrics |
| Redis | 6379 | Caching, sessions |

### API Documentation

- **Swagger UI:** `http://localhost:8000/docs`
- **ReDoc:** `http://localhost:8000/redoc`
- **OpenAPI JSON:** `http://localhost:8000/openapi.json`

---

## Security Features

### Data Protection
- ✅ **Aadhar UID**: HMAC-SHA256 irreversible hash
- ✅ **Aadhar Encryption**: AES-256-GCM for original number
- ✅ **Password Hashing**: bcrypt with 12 rounds
- ✅ **JWT Tokens**: HS256 with 30-min access, 7-day refresh
- ✅ **TLS/HTTPS**: All communications encrypted (Cloudflare)
- ✅ **RBAC**: Role-based endpoint access control

### Privacy
- ✅ **Consent System**: Patient controls all data access
- ✅ **Audit Logs**: All access tracked with timestamp, IP
- ✅ **Data Minimization**: Only necessary fields collected
- ✅ **Right to Delete**: Patients can delete their data

### Compliance
- HIPAA-compliant architecture
- GDPR-ready data handling
- India DPDPA alignment

---

## Error Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 204 | No Content (delete success) |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (invalid/missing token) |
| 403 | Forbidden (no permission) |
| 404 | Not Found |
| 409 | Conflict (duplicate entry) |
| 422 | Unprocessable Entity (invalid data) |
| 500 | Internal Server Error |

---

## Performance

### Optimizations
- **Comprehensive Endpoint**: 73% faster (1 call vs 5+)
- **MongoDB Indexes**: Sub-second queries on 30K+ metrics
- **Redis Caching**: Session data, rate limiting
- **Connection Pooling**: PostgreSQL, MongoDB connections reused
- **Async Operations**: FastAPI async/await throughout

### Capacity
- Tested with 30,186 health metrics (Apple Watch)
- Handles 100+ concurrent requests
- Scales horizontally with Docker Swarm/Kubernetes

---

## Summary

CloudCare backend provides a complete healthcare management API with:
- ✅ **Aadhar-based universal patient ID** (privacy-preserving)
- ✅ **Dual database architecture** (PostgreSQL + MongoDB)
- ✅ **JWT authentication** with RBAC
- ✅ **Wearable integration** (Apple Health, 30K+ metrics tested)
- ✅ **Patient consent system** (granular, time-limited)
- ✅ **Cross-facility records** (document portability)
- ✅ **Multi-hospital doctor support** (many-to-many relationships)
- ✅ **Hospital resource tracking** (oxygen, ventilators, ambulances, blood bags)
- ✅ **Docker deployment** (production-ready)

**Production URL:** `https://cloudcare.pipfactor.com/api/v1`  
**Swagger Docs:** `https://cloudcare.pipfactor.com/docs`

### Demo Hospitals

5 demo hospitals are available for testing:

| Hospital | Email | Password | Code |
|----------|-------|----------|------|
| Apollo Hospital Bangalore | hospital1@gmail.com | passme@123 | HC-B2631D |
| Manipal Hospital Whitefield | hospital2@gmail.com | passme@123 | HC-L9MLES |
| Fortis Hospital Bangalore | hospital3@gmail.com | passme@123 | HC-E8O2O2 |
| Columbia Asia Hospital Hebbal | hospital4@gmail.com | passme@123 | HC-JXOTB9 |
| Sakra World Hospital | hospital5@gmail.com | passme@123 | HC-A1NRS2 |

---

*Last Updated: November 2025 | Version 1.0*
