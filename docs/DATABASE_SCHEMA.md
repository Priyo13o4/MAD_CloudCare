# CloudCare Database Schema Documentation

**Version:** 1.0  
**Last Updated:** November 2025  
**Database Systems:** PostgreSQL 15+ | MongoDB 6.0+

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [PostgreSQL Schema](#postgresql-schema)
- [MongoDB Schema](#mongodb-schema)
- [Relationships](#relationships)
- [Indexes](#indexes)
- [Data Security](#data-security)

---

## Overview

CloudCare uses a **dual-database architecture** for optimal performance and scalability:

| Database | Purpose | Count |
|----------|---------|-------|
| **PostgreSQL** | Structured data, relationships, auth | 20 tables |
| **MongoDB** | Health metrics, time-series data | 1 collection (30K+ documents) |

**Why Dual Databases?**
- **PostgreSQL**: ACID compliance for critical data (auth, consents), strong relational integrity
- **MongoDB**: High write throughput for streaming health data, flexible schema for diverse metrics

---

## Architecture

```
CloudCare Data Layer
│
├── PostgreSQL (cloudcare_db)
│   ├── Authentication & Users
│   ├── Patient Profiles (Aadhar UID)
│   ├── Doctor & Hospital Profiles
│   ├── Consents & Permissions
│   ├── Medical Records Metadata
│   ├── Device Pairings
│   ├── Appointments
│   └── Audit Logs
│
└── MongoDB (cloudcare_wearables)
    └── health_metrics (30,186 documents)
        ├── Individual readings (minute-level granularity)
        ├── 7+ metric types
        └── Compound unique index for deduplication
```

---

## PostgreSQL Schema

### Enums

CloudCare uses several enums for type safety and data consistency:

#### `UserRole`
User account roles in the system:
- `PATIENT` - Patient account (access to own health data)
- `DOCTOR` - Doctor account (access to assigned patients with consent)
- `HOSPITAL_ADMIN` - Hospital administrator (access to hospital resources)
- `SYSTEM_ADMIN` - System administrator (logs access only)

#### `PatientStatus`
Patient status in doctor-patient relationship (in `doctor_patients` table):
- `LOCKED` - No access (consent pending/denied/revoked) - name only visible
- `ACTIVE` - Active patient with consent - full access granted
- `STABLE` - Stable condition - full access, routine monitoring
- `MONITORING` - Under observation - full access, enhanced monitoring
- `CRITICAL` - Critical condition - full access, immediate attention

#### `ConsentStatus`
Consent request status:
- `PENDING` - Request submitted, awaiting patient response
- `APPROVED` - Patient approved, doctor gains access
- `DENIED` - Patient denied, doctor remains locked
- `REVOKED` - Consent revoked by patient or doctor

#### `RecordType`
Medical document categories:
- `LAB_REPORT` - Laboratory test results
- `PRESCRIPTION` - Medication prescriptions
- `CONSULTATION` - Doctor consultation notes
- `IMAGING` - X-rays, CT scans, MRI, etc.
- `GENERAL` - Other medical documents

#### `ActivityType`
Activity feed event types:
- `RECORD_SHARED` - Medical record shared with doctor/facility
- `CONSENT_REQUEST` - New consent request received
- `CONSENT_APPROVED` - Consent approved by patient
- `CONSENT_REVOKED` - Consent revoked
- `DATA_SYNCED` - Health data synced from device
- `DOCUMENT_UPLOADED` - New medical document uploaded
- `APPOINTMENT_SCHEDULED` - Appointment scheduled
- `EMERGENCY_ALERT` - Emergency health alert triggered

---

### 1. Users & Authentication

#### `users` Table
**Purpose:** User accounts for all roles (patients, doctors, hospitals)

| Column | Type | Description |
|--------|------|-------------|
| `id` | uuid | Primary key |
| `email` | varchar(255) | Unique email address |
| `password_hash` | varchar(255) | bcrypt hashed password (12 rounds) |
| `role` | UserRole | PATIENT, DOCTOR, HOSPITAL_ADMIN, SYSTEM_ADMIN |
| `is_active` | boolean | Account active status |
| `created_at` | timestamp | Registration date |
| `updated_at` | timestamp | Last profile update |

**Relationships:**
- One-to-one with `patients`, `doctors`, or `hospitals`

**Indexes:**
- PRIMARY KEY: `id`
- UNIQUE: `email`

---

### 2. Patient Data

#### `patients` Table
**Purpose:** Patient profiles with Aadhar-based universal ID

| Column | Type | Description |
|--------|------|-------------|
| `id` | uuid | Primary key |
| `user_id` | uuid | Foreign key → users.id |
| `aadhar_uid` | varchar(64) | **HMAC-SHA256 hash (unique)** |
| `encrypted_aadhar` | text | AES-256 encrypted original Aadhar |
| `aadhar_verified` | boolean | Verification status |
| `aadhar_verified_at` | timestamp | Verification timestamp |
| `first_name` | varchar(255) | Patient first name |
| `middle_name` | varchar(255) | Optional middle name |
| `last_name` | varchar(255) | Patient last name |
| `date_of_birth` | date | DOB (for age calculation) |
| `gender` | varchar(20) | MALE, FEMALE, OTHER |
| `blood_group` | varchar(10) | O+, A+, B+, AB+, O-, A-, B-, AB- |
| `phone_primary` | varchar(15) | Primary contact |
| `phone_secondary` | varchar(15) | Alternate contact |
| `email` | varchar(255) | Patient email |
| `address_line1` | text | Address line 1 |
| `address_line2` | text | Address line 2 (optional) |
| `city` | varchar(100) | City |
| `state` | varchar(100) | State |
| `postal_code` | varchar(10) | Postal/ZIP code |
| `country` | varchar(100) | Country (default: India) |
| `emergency_contact_name` | varchar(255) | Emergency contact name |
| `emergency_contact_phone` | varchar(15) | Emergency phone |
| `emergency_contact_relation` | varchar(100) | Relationship |
| `height_cm` | float | Height in centimeters |
| `weight_kg` | float | Weight in kilograms |
| `allergies` | text | JSON array as string |
| `chronic_conditions` | text | JSON array as string |
| `current_medications` | text | JSON array as string |
| `insurance_provider` | varchar(255) | Insurance company |
| `insurance_policy_no` | varchar(100) | Policy number |
| `insurance_valid_until` | date | Policy expiry |
| `created_at` | timestamp | Registration date |
| `updated_at` | timestamp | Last update |

**Relationships:**
- Many-to-one with `users` (via `user_id`)
- One-to-many with `medical_records`
- One-to-many with `wearable_devices`
- One-to-many with `consents`
- One-to-many with `activities`
- Many-to-many with `doctors` (via `doctor_patients`)

**Indexes:**
- PRIMARY KEY: `id`
- UNIQUE: `user_id`, `aadhar_uid`
- INDEX: `phone_primary`, `aadhar_uid`

---

### 3. Doctor Data

#### `doctors` Table
**Purpose:** Doctor profiles with professional credentials

| Column | Type | Description |
|--------|------|-------------|
| `id` | uuid | Primary key |
| `user_id` | uuid | Foreign key → users.id |
| `medical_license_no` | varchar(100) | **Unique medical license** |
| `registration_year` | integer | Year of registration |
| `registration_state` | varchar(100) | State medical council |
| `first_name` | varchar(255) | Doctor first name |
| `middle_name` | varchar(255) | Optional middle name |
| `last_name` | varchar(255) | Doctor last name |
| `title` | varchar(20) | Dr., Prof. Dr., etc. |
| `date_of_birth` | date | DOB |
| `gender` | varchar(20) | Gender |
| `phone_primary` | varchar(15) | Primary contact |
| `phone_secondary` | varchar(15) | Alternate contact |
| `email_professional` | varchar(255) | Professional email |
| `address_line1` | text | Address |
| `city` | varchar(100) | City |
| `state` | varchar(100) | State |
| `postal_code` | varchar(10) | Postal code |
| `country` | varchar(100) | Country |
| `specialization` | varchar(255) | Primary specialization |
| `sub_specialization` | varchar(255) | Sub-specialty (optional) |
| `qualifications` | text | JSON array: ["MBBS", "MD"] |
| `experience_years` | integer | Years of practice |
| `languages_spoken` | text | JSON array: ["English", "Hindi"] |
| `consultation_fee` | float | Fee in INR |
| `available_for_emergency` | boolean | Emergency availability |
| `telemedicine_enabled` | boolean | Telemedicine support |
| `hospital_id` | uuid | Foreign key → hospitals.id |
| `hospital_code_input` | varchar(20) | Code entered during signup |
| `is_verified` | boolean | Verification status |
| `verified_at` | timestamp | Verification date |
| `is_active` | boolean | Active status |
| `created_at` | timestamp | Registration date |
| `updated_at` | timestamp | Last update |

**Relationships:**
- Many-to-one with `users` (via `user_id`)
- Many-to-one with `hospitals` (via `hospital_id`)
- Many-to-many with `patients` (via `doctor_patients`)
- One-to-many with `appointments`
- One-to-many with `patient_records`

**Indexes:**
- PRIMARY KEY: `id`
- UNIQUE: `user_id`, `medical_license_no`
- INDEX: `phone_primary`, `specialization`

---

#### `doctor_patients` Table
**Purpose:** Doctor-patient assignments with access control

| Column | Type | Description |
|--------|------|-------------|
| `id` | uuid | Primary key |
| `doctor_id` | uuid | Foreign key → doctors.id |
| `patient_id` | uuid | Foreign key → patients.id |
| `status` | PatientStatus | **LOCKED, ACTIVE, STABLE, MONITORING, CRITICAL** |
| `condition` | text | Medical condition description |
| `next_appointment` | timestamp | Next scheduled appointment |
| `last_visit` | timestamp | Last consultation date |
| `emergency_flag` | boolean | Emergency indicator |
| `assigned_at` | timestamp | Assignment date |

**Status Meanings:**
- `LOCKED`: No access to patient data (consent pending/denied)
- `ACTIVE`: Full access granted (consent approved)
- `STABLE`: Patient stable, full access
- `MONITORING`: Patient under observation, full access
- `CRITICAL`: Patient critical, full access

**Relationships:**
- Many-to-one with `doctors`
- Many-to-one with `patients`

**Indexes:**
- PRIMARY KEY: `id`
- UNIQUE: `(doctor_id, patient_id)`

---

### 4. Hospital Data

#### `hospitals` Table
**Purpose:** Hospital/clinic profiles

| Column | Type | Description |
|--------|------|-------------|
| `id` | uuid | Primary key |
| `user_id` | uuid | Foreign key → users.id |
| `name` | varchar(255) | Hospital name |
| `registration_no` | varchar(100) | Government registration |
| `license_no` | varchar(100) | Healthcare license |
| `accreditation` | varchar(100) | NABH, JCI, etc. |
| `hospital_code` | varchar(20) | **Auto-generated unique code (HC-XXXXXX)** |
| `facility_type` | varchar(100) | MULTI_SPECIALTY_HOSPITAL, CLINIC, etc. |
| `specializations` | text | JSON array |
| `phone_primary` | varchar(15) | Primary contact |
| `phone_emergency` | varchar(15) | Emergency line |
| `email` | varchar(255) | Hospital email |
| `website` | varchar(255) | Website URL |
| `address_line1` | text | Address |
| `city` | varchar(100) | City |
| `state` | varchar(100) | State |
| `postal_code` | varchar(10) | Postal code |
| `country` | varchar(100) | Country |
| `latitude` | float | GPS coordinate |
| `longitude` | float | GPS coordinate |
| `total_beds` | integer | Total bed capacity |
| `available_beds` | integer | Currently available |
| `icu_beds` | integer | ICU beds |
| `emergency_beds` | integer | Emergency beds |
| `operation_theatres` | integer | Operation theatres |
| `oxygen_cylinders` | integer | Oxygen cylinders |
| `ventilators` | integer | Ventilators |
| `ambulances` | integer | Ambulances |
| `blood_bags` | integer | Blood units |
| `has_emergency` | boolean | Emergency services |
| `has_ambulance` | boolean | Ambulance service |
| `has_pharmacy` | boolean | Pharmacy |
| `has_lab` | boolean | Lab services |
| `has_blood_bank` | boolean | Blood bank |
| `telemedicine_enabled` | boolean | Telemedicine |
| `is_active` | boolean | Active status |
| `is_verified` | boolean | Verification status |
| `verified_at` | timestamp | Verification date |
| `created_at` | timestamp | Registration date |
| `updated_at` | timestamp | Last update |

**Relationships:**
- One-to-one with `users`
- One-to-many with `doctors`
- One-to-many with `departments`
- One-to-many with `hospital_resources`
- One-to-many with `emergency_cases`

**Indexes:**
- PRIMARY KEY: `id`
- UNIQUE: `user_id`, `registration_no`, `license_no`, `hospital_code`
- INDEX: `(city, state)`, `facility_type`

---

### 5. Consent System

#### `consents` Table
**Purpose:** Patient-controlled data access permissions

| Column | Type | Description |
|--------|------|-------------|
| `id` | uuid | Primary key |
| `patient_id` | uuid | Foreign key → patients.id |
| `facility_name` | varchar(255) | Requesting facility |
| `request_type` | varchar(100) | Type of access requested |
| `description` | text | Request description |
| `status` | ConsentStatus | **PENDING, APPROVED, DENIED, REVOKED** |
| `requested_at` | timestamp | Request date |
| `responded_at` | timestamp | Response date |
| `expires_at` | timestamp | Expiry date (optional) |

**Status Flow:**
- `PENDING` → Patient hasn't responded yet
- `APPROVED` → Patient approved (doctor gets ACTIVE status)
- `DENIED` → Patient denied (doctor remains LOCKED)
- `REVOKED` → Patient or doctor revoked (doctor goes to LOCKED)

**Relationships:**
- Many-to-one with `patients`

**Indexes:**
- PRIMARY KEY: `id`
- INDEX: `patient_id`, `status`

---

### 6. Wearable Devices

#### `wearable_devices` Table
**Purpose:** Device registration and sync tracking

| Column | Type | Description |
|--------|------|-------------|
| `id` | uuid | Primary key |
| `patient_id` | uuid | Foreign key → patients.id |
| `name` | varchar(255) | Device name ("Apple Watch") |
| `type` | varchar(50) | apple_watch, fitbit, etc. |
| `device_id` | varchar(255) | **External device identifier (unique)** |
| `is_connected` | boolean | Connection status |
| `battery_level` | integer | Battery percentage |
| `last_sync_time` | timestamp | Last data sync |
| `data_points_synced` | integer | Total metrics synced |
| `source_version` | varchar(100) | watchOS/iOS version |
| `created_at` | timestamp | Registration date |
| `updated_at` | timestamp | Last update |

**Relationships:**
- Many-to-one with `patients`

**Indexes:**
- PRIMARY KEY: `id`
- UNIQUE: `device_id`
- INDEX: `patient_id`

---

#### `device_pairings` Table
**Purpose:** iOS-Android device pairing (QR code)

| Column | Type | Description |
|--------|------|-------------|
| `id` | uuid | Primary key |
| `ios_user_id` | varchar(255) | iOS app user ID |
| `ios_device_id` | varchar(255) | Apple device identifier |
| `android_user_id` | varchar(255) | Android app user ID |
| `device_name` | varchar(255) | Device name |
| `device_type` | varchar(50) | apple_watch, iphone |
| `pairing_code` | text | QR code data |
| `paired_at` | timestamp | Pairing date |
| `is_active` | boolean | Pairing active |
| `last_sync_at` | timestamp | Last sync |
| `total_metrics` | integer | Total synced metrics |

**Relationships:**
- None (cross-platform linking)

**Indexes:**
- PRIMARY KEY: `id`
- UNIQUE: `(ios_device_id, android_user_id)`
- INDEX: `android_user_id`, `ios_device_id`

---

### 7. Medical Records

#### `medical_records` Table
**Purpose:** Document metadata (files stored in MongoDB GridFS)

| Column | Type | Description |
|--------|------|-------------|
| `id` | uuid | Primary key |
| `patient_id` | uuid | Foreign key → patients.id |
| `facility_id` | uuid | Foreign key → facilities.id (optional) |
| `title` | varchar(255) | Document title |
| `description` | text | Document description |
| `date` | date | Document date |
| `record_type` | RecordType | LAB_REPORT, PRESCRIPTION, CONSULTATION, IMAGING, GENERAL |
| `file_url` | text | File storage URL |
| `created_at` | timestamp | Upload date |

**Relationships:**
- Many-to-one with `patients`
- Many-to-one with `facilities` (optional)

**Indexes:**
- PRIMARY KEY: `id`
- INDEX: `patient_id`, `record_type`

---

### 8. Activities & Audit

#### `activities` Table
**Purpose:** Activity feed for patients

| Column | Type | Description |
|--------|------|-------------|
| `id` | uuid | Primary key |
| `patient_id` | uuid | Foreign key → patients.id (optional) |
| `title` | varchar(255) | Activity title |
| `description` | text | Activity description |
| `timestamp` | timestamp | Activity time |
| `type` | ActivityType | RECORD_SHARED, CONSENT_REQUEST, DATA_SYNCED, etc. |
| `metadata` | text | JSON metadata |

**Relationships:**
- Many-to-one with `patients` (optional)

---

#### `audit_logs` Table
**Purpose:** Complete audit trail of all data access

| Column | Type | Description |
|--------|------|-------------|
| `id` | uuid | Primary key |
| `user_id` | varchar(255) | User who performed action |
| `action` | varchar(255) | Action performed |
| `resource` | varchar(255) | Resource accessed |
| `details` | text | JSON details |
| `ip_address` | varchar(50) | Client IP |
| `timestamp` | timestamp | Action time |

**Indexes:**
- PRIMARY KEY: `id`
- INDEX: `user_id`, `timestamp`

---

### 9. Additional Tables

**Other important tables in PostgreSQL:**
- `appointments` - Doctor appointments
- `emergency_alerts` - Critical health alerts
- `emergency_cases` - Hospital emergency admissions
- `departments` - Hospital departments
- `hospital_resources` - Resource tracking (beds, equipment)
- `facilities` - Linked facilities (clinics, labs)
- `patient_records` - Doctor consultation notes
- `document_requests` - Cross-facility record requests

---

## MongoDB Schema

### `health_metrics` Collection
**Purpose:** Individual health metric readings (not aggregated)

**Document Structure:**
```json
{
  "_id": ObjectId("674b2c5e1f8d9a3e4c5b6a7d"),
  "patient_id": "3228128A-7110-4D47-8EDB-3A9160E3808A",
  "device_id": "207791ED-2518-485D-B4D8-...",
  "metric_type": "heart_rate",
  "value": 72.5,
  "unit": "bpm",
  "timestamp": ISODate("2025-11-19T10:30:00.000Z"),
  "source": "Apple Watch Series 9",
  "source_version": "watchOS 11.1",
  "metadata": {
    "confidence": "high",
    "activity_type": "walking"
  },
  "created_at": ISODate("2025-11-19T10:31:00.000Z")
}
```

**Supported `metric_type` Values:**
- `heart_rate` - Heart rate (bpm)
- `resting_heart_rate` - Resting heart rate (bpm)
- `steps` - Step count (count)
- `calories` - Active energy burned (kcal)
- `distance` - Walking/running distance (meters)
- `flights_climbed` - Flights climbed (count)
- `vo2_max` - VO2 Max (mL/kg/min)
- `sleep_analysis` - Sleep duration (minutes)
- `blood_oxygen` - Blood oxygen saturation (percentage)
- `blood_pressure_systolic` - Systolic BP (mmHg)
- `blood_pressure_diastolic` - Diastolic BP (mmHg)

**Count:** 30,186 documents (tested with real Apple Watch data)

**Indexes:**
```javascript
// Compound unique index for deduplication
db.health_metrics.createIndex(
  {
    "patient_id": 1,
    "device_id": 1,
    "metric_type": 1,
    "timestamp": 1
  },
  { unique: true }
);

// Time-series queries
db.health_metrics.createIndex({ "patient_id": 1, "timestamp": -1 });
db.health_metrics.createIndex({ "metric_type": 1, "timestamp": -1 });
db.health_metrics.createIndex({ "timestamp": -1 });
```

**Aggregation Pipeline Example (Hourly):**
```javascript
db.health_metrics.aggregate([
  { $match: {
      patient_id: "uuid",
      metric_type: "heart_rate",
      timestamp: { $gte: ISODate("2025-11-19T00:00:00Z") }
  }},
  { $group: {
      _id: {
        hour: { $dateToString: { format: "%Y-%m-%d %H:00", date: "$timestamp" }}
      },
      avg_value: { $avg: "$value" },
      min_value: { $min: "$value" },
      max_value: { $max: "$value" },
      count: { $sum: 1 }
  }},
  { $sort: { "_id.hour": 1 }}
]);
```

---

## Relationships

### ER Diagram (Simplified)

```
users (1) ←─────→ (1) patients
                      ├─→ (n) medical_records
                      ├─→ (n) wearable_devices
                      ├─→ (n) consents
                      ├─→ (n) activities
                      └─→ (n) doctor_patients ←─ (n) doctors
                                                      ↓
                                                  hospitals
```

### Key Relationships

**One-to-One:**
- `users` ↔ `patients`
- `users` ↔ `doctors`
- `users` ↔ `hospitals`

**One-to-Many:**
- `patients` → `medical_records`
- `patients` → `wearable_devices`
- `patients` → `consents`
- `patients` → `activities`
- `hospitals` → `doctors`
- `hospitals` → `departments`
- `hospitals` → `emergency_cases`

**Many-to-Many:**
- `patients` ↔ `doctors` (via `doctor_patients`)
  - Status controls access level (LOCKED vs ACTIVE)

**Cross-Database:**
- `patients.id` → MongoDB `health_metrics.patient_id`
- `wearable_devices.id` → MongoDB `health_metrics.device_id`

---

## Indexes

### PostgreSQL Indexes

**Performance-Critical Indexes:**
```sql
-- Patient lookups
CREATE INDEX idx_patients_aadhar_uid ON patients(aadhar_uid);
CREATE INDEX idx_patients_phone ON patients(phone_primary);

-- Doctor lookups
CREATE INDEX idx_doctors_license ON doctors(medical_license_no);
CREATE INDEX idx_doctors_specialization ON doctors(specialization);

-- Hospital lookups
CREATE INDEX idx_hospitals_location ON hospitals(city, state);
CREATE INDEX idx_hospitals_code ON hospitals(hospital_code);

-- Consent filtering
CREATE INDEX idx_consents_patient_status ON consents(patient_id, status);

-- Audit logs
CREATE INDEX idx_audit_user_time ON audit_logs(user_id, timestamp);

-- Device sync
CREATE INDEX idx_devices_patient ON wearable_devices(patient_id);
CREATE INDEX idx_devices_sync ON wearable_devices(last_sync_time);
```

### MongoDB Indexes

**Deduplication & Performance:**
```javascript
// Primary compound unique index
db.health_metrics.createIndex(
  { patient_id: 1, device_id: 1, metric_type: 1, timestamp: 1 },
  { unique: true }
);

// Patient time-series queries
db.health_metrics.createIndex({ patient_id: 1, timestamp: -1 });

// Metric type queries
db.health_metrics.createIndex({ metric_type: 1, timestamp: -1 });

// Time-based queries
db.health_metrics.createIndex({ timestamp: -1 });
```

---

## Data Security

### Aadhar UID System

**Generation:**
```python
import hmac, hashlib

# Generate irreversible UID
aadhar_uid = hmac.new(
    AADHAR_HMAC_SECRET.encode(),
    aadhar_number.encode(),
    hashlib.sha256
).hexdigest()  # 64-character hex string
```

**Properties:**
- ✅ **Irreversible**: Cannot recover Aadhar from UID
- ✅ **Deterministic**: Same Aadhar → same UID
- ✅ **Unique**: Different Aadhar → different UID
- ✅ **Privacy-preserving**: UID doesn't reveal identity

**Storage:**
- `aadhar_uid`: Indexed, queryable (HMAC-SHA256 hash)
- `encrypted_aadhar`: Encrypted backup (AES-256-GCM)

### Password Security
- **Hashing**: bcrypt with 12 rounds
- **Salt**: Automatically included in bcrypt
- **Truncation**: Max 72 bytes (bcrypt limit)

### JWT Tokens
- **Algorithm**: HS256
- **Access Token**: 30 minutes
- **Refresh Token**: 7 days
- **Payload**: User ID, role, expiry

### Data Encryption
- **At Rest**: Database-level encryption
- **In Transit**: TLS 1.3 (Cloudflare)
- **Aadhar**: AES-256-GCM
- **Passwords**: bcrypt (irreversible)

---

## Performance

### Query Optimization

**PostgreSQL:**
- Compound indexes on frequently queried columns
- Foreign key indexes for joins
- Partial indexes for status filtering

**MongoDB:**
- Compound unique index prevents duplicates
- Covered queries (all fields in index)
- Aggregation pipeline optimization

### Capacity

**Tested Scale:**
- **Patients**: Unlimited (UUID-based)
- **Health Metrics**: 30,186 documents tested, scalable to millions
- **Query Performance**: Sub-second queries on 30K+ documents

---

## Migrations

### Adding New Tables
```bash
# 1. Update Prisma schema
# prisma/schema.prisma

# 2. Create migration
prisma migrate dev --name add_new_table

# 3. Apply to production
prisma migrate deploy
```

### MongoDB Schema Changes
No migrations needed (schema-less), but index changes:
```javascript
// Add new index
db.health_metrics.createIndex({ "new_field": 1 });

// Drop old index
db.health_metrics.dropIndex("old_index_name");
```

---

## Summary

CloudCare database schema provides:
- ✅ **Dual-database architecture** (PostgreSQL + MongoDB)
- ✅ **20 PostgreSQL tables** (structured data, relationships)
- ✅ **1 MongoDB collection** (30K+ health metrics)
- ✅ **Aadhar-based UIDs** (HMAC-SHA256, privacy-preserving)
- ✅ **Consent system** (patient-controlled access)
- ✅ **Device pairing** (iOS-Android linking)
- ✅ **Optimized indexes** (sub-second queries)
- ✅ **Security layers** (encryption, hashing, audit logs)

**PostgreSQL:** `cloudcare_db` (localhost:5432)  
**MongoDB:** `cloudcare_wearables` (localhost:27017)

---

*Last Updated: November 2025 | Version 1.0*
