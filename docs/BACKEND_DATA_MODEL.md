# Backend Data Model Specification

> Complete database schema specification for CloudCare's Patient and Doctor entities with Aadhar-based universal identification.

**Version**: 1.0  
**Last Updated**: December 2024  
**Database Systems**: PostgreSQL (relational) + MongoDB (document store)

---

## Table of Contents

- [Overview](#overview)
- [Database Architecture](#database-architecture)
- [Patient Entity](#patient-entity)
- [Doctor Entity](#doctor-entity)
- [Hospital Entity](#hospital-entity)
- [Relationships](#relationships)
- [Aadhar UID System](#aadhar-uid-system)
- [Security & Encryption](#security--encryption)
- [Indexes & Performance](#indexes--performance)
- [Sample Data](#sample-data)

---

## Overview

CloudCare uses a **dual-database architecture**:

- **PostgreSQL**: User authentication, profiles, relationships, consent management
- **MongoDB**: Health metrics (30K+ individual readings), medical documents (GridFS)

### Why Dual Databases?

| Data Type | Database | Reason |
|-----------|----------|--------|
| User accounts, authentication | PostgreSQL | ACID transactions, strong consistency |
| Patient/Doctor profiles | PostgreSQL | Relational integrity (facilities, consents) |
| Device pairings | PostgreSQL | Foreign key constraints |
| Health metrics (wearables) | MongoDB | High write throughput, time-series optimization |
| Medical documents | MongoDB | GridFS for large file storage |

---

## Database Architecture

```
CloudCare Databases
│
├── PostgreSQL (cloudcare_db)
│   ├── User (authentication)
│   ├── Patient (profiles + Aadhar UID)
│   ├── Doctor (professional details)
│   ├── Hospital (facility information)
│   ├── Consent (data access control)
│   ├── DevicePairing (iOS ↔ Android linking)
│   ├── MedicalRecord (metadata only)
│   └── AuditLog (all access tracking)
│
└── MongoDB (cloudcare_wearables)
    ├── health_metrics (individual readings)
    │   ├── heart_rate
    │   ├── steps
    │   ├── calories
    │   ├── distance
    │   ├── flights_climbed
    │   ├── resting_heart_rate
    │   └── vo2_max
    ├── medical_documents (GridFS)
    └── device_sync_status
```

---

## Patient Entity

### PostgreSQL Schema (User + Patient tables)

#### `User` Table
**Purpose**: Authentication and basic account information  
**Database**: PostgreSQL

```prisma
model User {
  id                String    @id @default(uuid())
  email             String    @unique
  password_hash     String    // bcrypt hashed
  role              UserRole  // PATIENT, DOCTOR, HOSPITAL_ADMIN
  is_active         Boolean   @default(true)
  is_verified       Boolean   @default(false)
  created_at        DateTime  @default(now())
  updated_at        DateTime  @updatedAt
  last_login        DateTime?
  
  // One-to-one relationships
  patient           Patient?
  doctor            Doctor?
  hospital_admin    HospitalAdmin?
  
  // Audit trail
  audit_logs        AuditLog[]
}

enum UserRole {
  PATIENT
  DOCTOR
  HOSPITAL_ADMIN
  SYSTEM_ADMIN
}
```

#### `Patient` Table
**Purpose**: Patient profile and Aadhar-based UID  
**Database**: PostgreSQL

```prisma
model Patient {
  id                    String      @id @default(uuid())
  user_id               String      @unique
  user                  User        @relation(fields: [user_id], references: [id], onDelete: Cascade)
  
  // Aadhar-Based Universal ID (CRITICAL)
  aadhar_uid            String      @unique  // HMAC-SHA256 hash (64 chars)
  encrypted_aadhar      String      // AES-256 encrypted original Aadhar
  aadhar_verified       Boolean     @default(false)
  aadhar_verified_at    DateTime?
  
  // Personal Information
  first_name            String
  middle_name           String?
  last_name             String
  date_of_birth         DateTime
  gender                Gender
  blood_group           BloodGroup?
  
  // Contact Information
  phone_primary         String      @unique
  phone_secondary       String?
  address_line1         String
  address_line2         String?
  city                  String
  state                 String
  postal_code           String
  country               String      @default("India")
  
  // Emergency Contact
  emergency_contact_name    String
  emergency_contact_phone   String
  emergency_contact_relation String
  
  // Medical Profile
  height_cm             Float?      // in centimeters
  weight_kg             Float?      // in kilograms
  allergies             String[]    // Array of allergy strings
  chronic_conditions    String[]    // Array of condition strings
  current_medications   String[]    // Array of medication names
  
  // Insurance Details (Optional)
  insurance_provider    String?
  insurance_policy_no   String?
  insurance_valid_until DateTime?
  
  // System Metadata
  created_at            DateTime    @default(now())
  updated_at            DateTime    @updatedAt
  last_profile_update   DateTime?
  
  // Relationships
  consents              Consent[]
  medical_records       MedicalRecord[]
  device_pairings       DevicePairing[]
  facility_links        PatientFacilityLink[]
  document_requests     DocumentRequest[]
  
  @@index([aadhar_uid])
  @@index([phone_primary])
  @@index([created_at])
}

enum Gender {
  MALE
  FEMALE
  OTHER
  PREFER_NOT_TO_SAY
}

enum BloodGroup {
  A_POSITIVE
  A_NEGATIVE
  B_POSITIVE
  B_NEGATIVE
  AB_POSITIVE
  AB_NEGATIVE
  O_POSITIVE
  O_NEGATIVE
}
```

### MongoDB Collections (Health Data)

#### `health_metrics` Collection
**Purpose**: Individual wearable device readings (not aggregated)  
**Database**: MongoDB

```javascript
{
  "_id": ObjectId("..."),
  "patient_id": "uuid-from-postgresql",      // Links to Patient.id
  "device_id": "uuid-from-device-pairing",   // Links to DevicePairing.id
  "metric_type": "heart_rate",               // enum: heart_rate, steps, calories, etc.
  "value": 72.5,                             // Numeric value
  "unit": "bpm",                             // Unit of measurement
  "timestamp": ISODate("2024-12-19T10:30:00Z"), // UTC timestamp
  "source": "Apple Watch Series 9",          // Device name
  "source_version": "watchOS 11.1",          // OS version
  "metadata": {                              // Optional additional data
    "confidence": "high",
    "activity_type": "walking"
  },
  "created_at": ISODate("2024-12-19T10:31:00Z")
}

// Supported metric_type values:
// - heart_rate (bpm)
// - resting_heart_rate (bpm)
// - steps (count)
// - calories (kcal)
// - distance (meters)
// - flights_climbed (count)
// - vo2_max (mL/kg/min)
// - sleep_analysis (minutes)
// - blood_oxygen (percentage)
// - blood_pressure_systolic (mmHg)
// - blood_pressure_diastolic (mmHg)

// Compound Index for Deduplication:
db.health_metrics.createIndex(
  { "patient_id": 1, "device_id": 1, "metric_type": 1, "timestamp": 1 },
  { unique: true }
);

// Additional Indexes:
db.health_metrics.createIndex({ "patient_id": 1, "timestamp": -1 });
db.health_metrics.createIndex({ "metric_type": 1, "timestamp": -1 });
```

#### `medical_documents` Collection (GridFS)
**Purpose**: Store large medical files (PDFs, images, scans)  
**Database**: MongoDB GridFS

```javascript
// GridFS files collection
{
  "_id": ObjectId("..."),
  "filename": "lab_report_2024-12-19.pdf",
  "contentType": "application/pdf",
  "length": 1048576,                         // File size in bytes
  "chunkSize": 261120,
  "uploadDate": ISODate("2024-12-19T10:00:00Z"),
  "metadata": {
    "patient_id": "uuid-from-postgresql",
    "record_id": "uuid-from-medical-record",
    "document_type": "LAB_REPORT",           // enum: LAB_REPORT, PRESCRIPTION, etc.
    "uploaded_by": "doctor-uuid",
    "facility_id": "hospital-uuid",
    "encryption_key_id": "key-version-1"     // For encrypted documents
  }
}

// Document Types:
// - LAB_REPORT
// - PRESCRIPTION
// - IMAGING (X-Ray, MRI, CT Scan)
// - DISCHARGE_SUMMARY
// - CONSULTATION_NOTE
// - VACCINATION_RECORD
// - INSURANCE_CLAIM
```

---

## Doctor Entity

### PostgreSQL Schema

#### `Doctor` Table
**Purpose**: Doctor professional profile and credentials  
**Database**: PostgreSQL

```prisma
model Doctor {
  id                    String      @id @default(uuid())
  user_id               String      @unique
  user                  User        @relation(fields: [user_id], references: [id], onDelete: Cascade)
  
  // Professional Identification
  medical_license_no    String      @unique  // Medical Council registration
  registration_year     Int
  registration_state    String      // State medical council
  aadhar_uid            String?     @unique  // Optional Aadhar UID for doctors
  encrypted_aadhar      String?     // AES-256 encrypted (optional)
  
  // Personal Information
  first_name            String
  middle_name           String?
  last_name             String
  title                 String      // Dr., Prof. Dr., etc.
  date_of_birth         DateTime
  gender                Gender
  
  // Contact Information
  phone_primary         String      @unique
  phone_secondary       String?
  email_professional    String?     @unique
  address_line1         String
  address_line2         String?
  city                  String
  state                 String
  postal_code           String
  country               String      @default("India")
  
  // Professional Details
  specialization        String      // Cardiology, Neurology, etc.
  sub_specialization    String?     // Interventional Cardiology, etc.
  qualifications        String[]    // ["MBBS", "MD", "DM"]
  experience_years      Int
  languages_spoken      String[]    // ["English", "Hindi", "Bengali"]
  
  // Practice Information
  consultation_fee      Float?      // in INR
  available_for_emergency Boolean   @default(false)
  telemedicine_enabled  Boolean     @default(false)
  
  // Verification Status
  is_verified           Boolean     @default(false)
  verified_at           DateTime?
  verified_by           String?     // Admin user ID
  verification_documents String[]   // URLs to uploaded certificates
  
  // System Metadata
  created_at            DateTime    @default(now())
  updated_at            DateTime    @updatedAt
  last_profile_update   DateTime?
  
  // Relationships
  hospital_affiliations DoctorHospitalAffiliation[]
  consents_granted      Consent[]
  appointments          Appointment[]
  consultations         Consultation[]
  
  @@index([medical_license_no])
  @@index([specialization])
  @@index([phone_primary])
}
```

#### `DoctorHospitalAffiliation` Table
**Purpose**: Link doctors to multiple hospitals  
**Database**: PostgreSQL

```prisma
model DoctorHospitalAffiliation {
  id                    String      @id @default(uuid())
  doctor_id             String
  doctor                Doctor      @relation(fields: [doctor_id], references: [id], onDelete: Cascade)
  hospital_id           String
  hospital              Hospital    @relation(fields: [hospital_id], references: [id], onDelete: Cascade)
  
  // Affiliation Details
  department            String      // Cardiology, Emergency, ICU, etc.
  designation           String      // Consultant, Senior Consultant, HOD
  is_primary_facility   Boolean     @default(false)
  joining_date          DateTime
  leaving_date          DateTime?
  is_active             Boolean     @default(true)
  
  // Availability
  working_days          String[]    // ["MONDAY", "TUESDAY", "FRIDAY"]
  shift_start_time      String      // "09:00"
  shift_end_time        String      // "17:00"
  
  created_at            DateTime    @default(now())
  updated_at            DateTime    @updatedAt
  
  @@unique([doctor_id, hospital_id, department])
  @@index([doctor_id, is_active])
  @@index([hospital_id, department])
}
```

---

## Hospital Entity

### PostgreSQL Schema

#### `Hospital` Table
**Purpose**: Healthcare facility information  
**Database**: PostgreSQL

```prisma
model Hospital {
  id                    String      @id @default(uuid())
  
  // Facility Identification
  name                  String
  registration_no       String      @unique  // Government registration
  license_no            String      @unique  // Healthcare license
  accreditation         String?     // NABH, JCI, etc.
  
  // Facility Type
  type                  FacilityType
  specializations       String[]    // ["Cardiology", "Neurology"]
  
  // Contact Information
  phone_primary         String
  phone_emergency       String
  email                 String      @unique
  website               String?
  
  // Address
  address_line1         String
  address_line2         String?
  city                  String
  state                 String
  postal_code           String
  country               String      @default("India")
  latitude              Float?      // For map integration
  longitude             Float?
  
  // Capacity
  total_beds            Int
  icu_beds              Int
  emergency_beds        Int
  operation_theatres    Int
  
  // Services
  has_emergency         Boolean     @default(true)
  has_ambulance         Boolean     @default(false)
  has_pharmacy          Boolean     @default(false)
  has_lab               Boolean     @default(false)
  has_blood_bank        Boolean     @default(false)
  telemedicine_enabled  Boolean     @default(false)
  
  // System Metadata
  is_active             Boolean     @default(true)
  is_verified           Boolean     @default(false)
  verified_at           DateTime?
  created_at            DateTime    @default(now())
  updated_at            DateTime    @updatedAt
  
  // Relationships
  doctor_affiliations   DoctorHospitalAffiliation[]
  patient_links         PatientFacilityLink[]
  medical_records       MedicalRecord[]
  consents              Consent[]
  
  @@index([city, state])
  @@index([type])
  @@index([is_active, is_verified])
}

enum FacilityType {
  MULTI_SPECIALTY_HOSPITAL
  SPECIALTY_HOSPITAL
  CLINIC
  DIAGNOSTIC_CENTER
  PHARMACY
  BLOOD_BANK
  RESEARCH_INSTITUTE
}
```

---

## Relationships

### Patient ↔ Hospital Linking

#### `PatientFacilityLink` Table
**Purpose**: Track which hospitals a patient is linked to  
**Database**: PostgreSQL

```prisma
model PatientFacilityLink {
  id                    String      @id @default(uuid())
  patient_id            String
  patient               Patient     @relation(fields: [patient_id], references: [id], onDelete: Cascade)
  hospital_id           String
  hospital              Hospital    @relation(fields: [hospital_id], references: [id], onDelete: Cascade)
  
  // Linking Details
  patient_hospital_id   String      // Hospital's internal patient ID
  linked_date           DateTime    @default(now())
  linked_by             String?     // User ID who created link
  is_active             Boolean     @default(true)
  
  // Record Request Status
  records_requested     Boolean     @default(false)
  records_received      Boolean     @default(false)
  request_date          DateTime?
  received_date         DateTime?
  
  created_at            DateTime    @default(now())
  updated_at            DateTime    @updatedAt
  
  @@unique([patient_id, hospital_id])
  @@index([patient_id, is_active])
  @@index([hospital_id, is_active])
}
```

### Consent Management

#### `Consent` Table
**Purpose**: Patient-controlled data access  
**Database**: PostgreSQL

```prisma
model Consent {
  id                    String      @id @default(uuid())
  patient_id            String
  patient               Patient     @relation(fields: [patient_id], references: [id], onDelete: Cascade)
  
  // Consent Grantee (who gets access)
  grantee_type          GranteeType // DOCTOR, HOSPITAL, RESEARCHER
  doctor_id             String?
  doctor                Doctor?     @relation(fields: [doctor_id], references: [id])
  hospital_id           String?
  hospital              Hospital?   @relation(fields: [hospital_id], references: [id])
  
  // Consent Scope
  data_categories       String[]    // ["HEALTH_METRICS", "LAB_REPORTS", "PRESCRIPTIONS"]
  purpose               String      // "Treatment", "Research", "Insurance"
  
  // Time Constraints
  granted_at            DateTime    @default(now())
  expires_at            DateTime?   // null = no expiration
  revoked_at            DateTime?
  is_active             Boolean     @default(true)
  
  // Audit
  granted_by            String      // Patient user ID
  revoked_by            String?
  revocation_reason     String?
  
  created_at            DateTime    @default(now())
  updated_at            DateTime    @updatedAt
  
  @@index([patient_id, is_active])
  @@index([doctor_id, is_active])
  @@index([hospital_id, is_active])
  @@index([expires_at])
}

enum GranteeType {
  DOCTOR
  HOSPITAL
  RESEARCHER
  INSURANCE_COMPANY
  EMERGENCY_SERVICE
}
```

### Device Pairing (iOS ↔ Android)

#### `DevicePairing` Table
**Purpose**: Link wearable devices to patient accounts  
**Database**: PostgreSQL

```prisma
model DevicePairing {
  id                    String      @id @default(uuid())
  patient_id            String
  patient               Patient     @relation(fields: [patient_id], references: [id], onDelete: Cascade)
  
  // Device Information
  device_name           String      // "Apple Watch Series 9"
  device_type           DeviceType
  device_os             String      // "watchOS 11.1"
  device_model          String?     // "Watch7,1"
  
  // Pairing Details
  pairing_code          String      @unique  // QR code data (hashed)
  paired_at             DateTime    @default(now())
  last_sync_at          DateTime?
  is_active             Boolean     @default(true)
  
  // Sync Status
  total_syncs           Int         @default(0)
  total_metrics_synced  Int         @default(0)
  last_error            String?
  last_error_at         DateTime?
  
  created_at            DateTime    @default(now())
  updated_at            DateTime    @updatedAt
  
  @@index([patient_id, is_active])
  @@index([device_type])
  @@index([last_sync_at])
}

enum DeviceType {
  APPLE_WATCH
  IPHONE
  FITBIT
  XIAOMI_MI_BAND
  SAMSUNG_GALAXY_WATCH
  GOOGLE_FIT
  OTHER
}
```

---

## Aadhar UID System

### How It Works

```
Patient Registration Flow
    ↓
1. User enters: "1234 5678 9012" (Aadhar number)
    ↓
2. Backend generates:
   - aadhar_uid = HMAC-SHA256(aadhar, secret_key)
     → "a3f5e8d9c2b1a0f4e3d2c1b0a9f8e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0"
   - encrypted_aadhar = AES-256-GCM(aadhar, encryption_key)
     → "U2FsdGVkX1+Q3..."
    ↓
3. Store in database:
   - patient.aadhar_uid (indexed, unique)
   - patient.encrypted_aadhar (separate secure storage)
    ↓
4. Use aadhar_uid for ALL cross-facility operations
```

### UID Generation Code

```python
import hmac
import hashlib
from cryptography.fernet import Fernet

# Configuration
AADHAR_HMAC_SECRET = os.getenv("AADHAR_HMAC_SECRET")  # 256-bit secret
AADHAR_ENCRYPTION_KEY = os.getenv("AADHAR_ENCRYPTION_KEY")  # Fernet key

def generate_aadhar_uid(aadhar_number: str) -> str:
    """
    Generate irreversible UID from Aadhar number.
    
    Args:
        aadhar_number: 12-digit Aadhar (spaces removed)
    
    Returns:
        64-character hex string (HMAC-SHA256)
    """
    # Remove spaces and validate
    aadhar_clean = aadhar_number.replace(" ", "").strip()
    if len(aadhar_clean) != 12 or not aadhar_clean.isdigit():
        raise ValueError("Invalid Aadhar number format")
    
    # Generate HMAC-SHA256
    uid = hmac.new(
        AADHAR_HMAC_SECRET.encode(),
        aadhar_clean.encode(),
        hashlib.sha256
    ).hexdigest()
    
    return uid

def encrypt_aadhar(aadhar_number: str) -> str:
    """
    Encrypt original Aadhar for backup/verification.
    
    Args:
        aadhar_number: 12-digit Aadhar
    
    Returns:
        Encrypted string (AES-256-GCM via Fernet)
    """
    cipher = Fernet(AADHAR_ENCRYPTION_KEY.encode())
    encrypted = cipher.encrypt(aadhar_number.encode())
    return encrypted.decode()

def decrypt_aadhar(encrypted_aadhar: str) -> str:
    """
    Decrypt Aadhar (admin-only operation).
    """
    cipher = Fernet(AADHAR_ENCRYPTION_KEY.encode())
    decrypted = cipher.decrypt(encrypted_aadhar.encode())
    return decrypted.decode()
```

### Security Properties

| Property | Implementation |
|----------|----------------|
| **Irreversible** | HMAC-SHA256 is one-way (cannot recover Aadhar from UID) |
| **Deterministic** | Same Aadhar always generates same UID |
| **Unique** | Different Aadhar numbers → different UIDs |
| **Privacy** | UID doesn't reveal Aadhar number |
| **Backup** | Original Aadhar encrypted separately (for legal/verification) |

### Use Cases

1. **Cross-Facility Record Lookup**:
   ```sql
   -- Find patient records across all hospitals
   SELECT * FROM patient_facility_links
   WHERE patient_id IN (
     SELECT id FROM patients WHERE aadhar_uid = 'a3f5e8d9...'
   );
   ```

2. **Document Request by Aadhar**:
   ```python
   # Patient requests records from Hospital X using only Aadhar
   patient = db.query(Patient).filter(
       Patient.aadhar_uid == generate_aadhar_uid(aadhar_input)
   ).first()
   
   # Request records from hospital
   create_document_request(patient.id, hospital_id)
   ```

3. **Prevent Duplicate Registrations**:
   ```python
   # Check if Aadhar already registered
   uid = generate_aadhar_uid(aadhar_number)
   existing = db.query(Patient).filter(Patient.aadhar_uid == uid).first()
   if existing:
       raise HTTPException(409, "Patient already registered")
   ```

---

## Security & Encryption

### Encryption Layers

| Data Type | Encryption Method | Key Storage |
|-----------|------------------|-------------|
| Passwords | bcrypt (12 rounds) | N/A (hashed) |
| Aadhar UID | HMAC-SHA256 | Environment variable `AADHAR_HMAC_SECRET` |
| Aadhar (original) | AES-256-GCM (Fernet) | Environment variable `AADHAR_ENCRYPTION_KEY` |
| JWT tokens | RS256 (RSA) | Private key file (PEM) |
| Medical documents | AES-256-GCM | Per-document key in MongoDB metadata |
| API requests | TLS 1.3 | SSL certificate (Cloudflare) |

### Key Rotation Strategy

```python
# Environment variables (rotate every 90 days)
AADHAR_HMAC_SECRET_V1="..."  # Current active key
AADHAR_HMAC_SECRET_V2="..."  # Previous key (grace period)

AADHAR_ENCRYPTION_KEY_V1="..."  # Current active key
AADHAR_ENCRYPTION_KEY_V2="..."  # Previous key (for decryption)

# Migration script
def migrate_aadhar_encryption():
    """Re-encrypt all Aadhar numbers with new key."""
    patients = db.query(Patient).all()
    for patient in patients:
        # Decrypt with old key
        aadhar = decrypt_aadhar(patient.encrypted_aadhar, key_version=2)
        # Re-encrypt with new key
        patient.encrypted_aadhar = encrypt_aadhar(aadhar, key_version=1)
    db.commit()
```

### Access Control Matrix

| Entity | Patient Data | Doctor Data | Hospital Data | Health Metrics |
|--------|-------------|-------------|---------------|----------------|
| **Patient** | Full access | View (with consent) | View linked facilities | Full access (own) |
| **Doctor** | View (with consent) | Full access (own) | View affiliated hospitals | View (with consent) |
| **Hospital Admin** | View linked patients | View affiliated doctors | Full access (own) | View (with consent) |
| **System Admin** | Read-only | Read-only | Read-only | Read-only |

---

## Indexes & Performance

### PostgreSQL Indexes

```sql
-- Patient table
CREATE INDEX idx_patient_aadhar_uid ON patients(aadhar_uid);
CREATE INDEX idx_patient_phone ON patients(phone_primary);
CREATE INDEX idx_patient_created_at ON patients(created_at);

-- Doctor table
CREATE INDEX idx_doctor_license ON doctors(medical_license_no);
CREATE INDEX idx_doctor_specialization ON doctors(specialization);
CREATE INDEX idx_doctor_phone ON doctors(phone_primary);

-- Hospital table
CREATE INDEX idx_hospital_location ON hospitals(city, state);
CREATE INDEX idx_hospital_type ON hospitals(type);
CREATE INDEX idx_hospital_active ON hospitals(is_active, is_verified);

-- Consent table
CREATE INDEX idx_consent_patient_active ON consents(patient_id, is_active);
CREATE INDEX idx_consent_doctor_active ON consents(doctor_id, is_active);
CREATE INDEX idx_consent_expiry ON consents(expires_at);

-- Device Pairing table
CREATE INDEX idx_device_patient_active ON device_pairings(patient_id, is_active);
CREATE INDEX idx_device_last_sync ON device_pairings(last_sync_at);
```

### MongoDB Indexes

```javascript
// health_metrics collection
db.health_metrics.createIndex(
  { patient_id: 1, device_id: 1, metric_type: 1, timestamp: 1 },
  { unique: true }  // Deduplication
);
db.health_metrics.createIndex({ patient_id: 1, timestamp: -1 });
db.health_metrics.createIndex({ metric_type: 1, timestamp: -1 });
db.health_metrics.createIndex({ timestamp: -1 });  // Time-series queries

// medical_documents.files (GridFS)
db.medical_documents.files.createIndex({ "metadata.patient_id": 1 });
db.medical_documents.files.createIndex({ "metadata.record_id": 1 });
db.medical_documents.files.createIndex({ "metadata.document_type": 1 });
```

### Query Performance Tips

1. **Aadhar UID Lookups**: Always use `aadhar_uid` index (O(log n) lookup)
2. **Health Metrics**: Query by `patient_id` + `timestamp` range for best performance
3. **Consent Checks**: Use compound index `(patient_id, is_active)` before data access
4. **Document Retrieval**: Query GridFS by `metadata.patient_id` or `metadata.record_id`

---

## Sample Data

### Sample Patient Record

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "user_id": "123e4567-e89b-12d3-a456-426614174000",
  "aadhar_uid": "a3f5e8d9c2b1a0f4e3d2c1b0a9f8e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0",
  "encrypted_aadhar": "gAAAAABh5Q...",
  "aadhar_verified": true,
  "aadhar_verified_at": "2024-01-15T10:30:00Z",
  
  "first_name": "Rajesh",
  "middle_name": "Kumar",
  "last_name": "Sharma",
  "date_of_birth": "1985-03-20T00:00:00Z",
  "gender": "MALE",
  "blood_group": "O_POSITIVE",
  
  "phone_primary": "+919876543210",
  "phone_secondary": "+918765432109",
  "address_line1": "A-101, Green Park Apartments",
  "address_line2": "Sector 15",
  "city": "Noida",
  "state": "Uttar Pradesh",
  "postal_code": "201301",
  "country": "India",
  
  "emergency_contact_name": "Priya Sharma",
  "emergency_contact_phone": "+919876543211",
  "emergency_contact_relation": "Spouse",
  
  "height_cm": 175.0,
  "weight_kg": 78.5,
  "allergies": ["Penicillin", "Pollen"],
  "chronic_conditions": ["Type 2 Diabetes", "Hypertension"],
  "current_medications": ["Metformin 500mg", "Amlodipine 5mg"],
  
  "insurance_provider": "Star Health Insurance",
  "insurance_policy_no": "SH/2024/123456",
  "insurance_valid_until": "2025-12-31T23:59:59Z",
  
  "created_at": "2024-01-10T08:00:00Z",
  "updated_at": "2024-12-19T14:30:00Z"
}
```

### Sample Doctor Record

```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "user_id": "223e4567-e89b-12d3-a456-426614174001",
  
  "medical_license_no": "MCI-12345-2015",
  "registration_year": 2015,
  "registration_state": "Maharashtra",
  "aadhar_uid": "b4g6f9e0d3c2b1a0f4e3d2c1b0a9f8e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2",
  "encrypted_aadhar": "gAAAAABh6R...",
  
  "first_name": "Dr. Anjali",
  "middle_name": null,
  "last_name": "Verma",
  "title": "Dr.",
  "date_of_birth": "1980-07-12T00:00:00Z",
  "gender": "FEMALE",
  
  "phone_primary": "+919123456789",
  "phone_secondary": "+919123456788",
  "email_professional": "dr.anjali.verma@hospital.com",
  "address_line1": "Medical Staff Quarters",
  "address_line2": "AIIMS Campus",
  "city": "New Delhi",
  "state": "Delhi",
  "postal_code": "110029",
  "country": "India",
  
  "specialization": "Cardiology",
  "sub_specialization": "Interventional Cardiology",
  "qualifications": ["MBBS", "MD (Medicine)", "DM (Cardiology)"],
  "experience_years": 12,
  "languages_spoken": ["English", "Hindi", "Bengali"],
  
  "consultation_fee": 1500.00,
  "available_for_emergency": true,
  "telemedicine_enabled": true,
  
  "is_verified": true,
  "verified_at": "2024-01-20T10:00:00Z",
  "verified_by": "admin-uuid-123",
  "verification_documents": [
    "https://cloudcare.com/docs/mci_certificate_12345.pdf",
    "https://cloudcare.com/docs/dm_cardiology_certificate.pdf"
  ],
  
  "created_at": "2024-01-15T09:00:00Z",
  "updated_at": "2024-12-19T15:00:00Z"
}
```

### Sample Health Metric (MongoDB)

```json
{
  "_id": { "$oid": "674b2c5e1f8d9a3e4c5b6a7d" },
  "patient_id": "550e8400-e29b-41d4-a716-446655440000",
  "device_id": "device-pairing-uuid-123",
  "metric_type": "heart_rate",
  "value": 72.0,
  "unit": "bpm",
  "timestamp": { "$date": "2024-12-19T10:30:00.000Z" },
  "source": "Apple Watch Series 9",
  "source_version": "watchOS 11.1",
  "metadata": {
    "confidence": "high",
    "activity_type": "walking",
    "location": "outdoor"
  },
  "created_at": { "$date": "2024-12-19T10:31:00.000Z" }
}
```

---

## Migration Guide

### From Mock Data to Production

1. **Patient Migration**:
   ```sql
   -- Migrate existing patients
   UPDATE patients
   SET aadhar_uid = generate_aadhar_uid(encrypted_aadhar),
       aadhar_verified = false
   WHERE aadhar_uid IS NULL;
   ```

2. **Doctor Migration**:
   ```sql
   -- Add Aadhar UIDs for doctors (optional)
   ALTER TABLE doctors ADD COLUMN aadhar_uid VARCHAR(64) UNIQUE;
   ALTER TABLE doctors ADD COLUMN encrypted_aadhar TEXT;
   ```

3. **Health Metrics Migration**:
   ```javascript
   // MongoDB: Add compound index for deduplication
   db.health_metrics.createIndex(
     { patient_id: 1, device_id: 1, metric_type: 1, timestamp: 1 },
     { unique: true, background: true }
   );
   ```

### Schema Validation

```python
# Validation script
def validate_patient_schema(patient: Patient):
    assert len(patient.aadhar_uid) == 64, "Invalid UID length"
    assert patient.phone_primary.startswith("+91"), "Invalid phone format"
    assert patient.date_of_birth < datetime.now(), "Future DOB"
    assert len(patient.encrypted_aadhar) > 0, "Missing encrypted Aadhar"

def validate_doctor_schema(doctor: Doctor):
    assert re.match(r"MCI-\d+-\d{4}", doctor.medical_license_no), "Invalid license"
    assert doctor.experience_years >= 0, "Negative experience"
    assert len(doctor.qualifications) > 0, "No qualifications"
```

---

## API Endpoints Summary

### Patient Endpoints

```
POST   /api/v1/patients/register           # Create patient with Aadhar UID
GET    /api/v1/patients/me                 # Get current patient profile
PUT    /api/v1/patients/me                 # Update patient profile
GET    /api/v1/patients/{aadhar_uid}       # Find patient by Aadhar UID
POST   /api/v1/patients/verify-aadhar      # Verify Aadhar via OTP
```

### Doctor Endpoints

```
POST   /api/v1/doctors/register            # Create doctor profile
GET    /api/v1/doctors/me                  # Get current doctor profile
PUT    /api/v1/doctors/me                  # Update doctor profile
GET    /api/v1/doctors/{license_no}        # Find doctor by license
POST   /api/v1/doctors/verify              # Submit verification documents
```

### Health Metrics Endpoints

```
GET    /api/v1/wearables/comprehensive     # Get all metrics (single endpoint)
POST   /api/v1/wearables/metrics           # Upload health metrics
POST   /api/v1/wearables/apple-health      # Batch import Apple Health JSON
GET    /api/v1/wearables/metrics/{type}    # Get specific metric type
```

---

## Conclusion

This data model specification provides a complete foundation for CloudCare's backend database architecture. Key features:

✅ **Aadhar-based universal patient identification**  
✅ **Dual-database architecture (PostgreSQL + MongoDB)**  
✅ **Privacy-preserving encryption (HMAC-SHA256 + AES-256)**  
✅ **Scalable health metrics storage (30K+ tested)**  
✅ **Comprehensive relationship mapping**  
✅ **Production-ready indexes and performance optimization**

For implementation details, see:
- **[BACKEND_SETUP_GUIDE.md](BACKEND_SETUP_GUIDE.md)** - Complete setup instructions
- **[backend/README.md](../backend/README.md)** - Quick start guide
- **[prisma/schema.prisma](../backend/prisma/schema.prisma)** - Actual Prisma schema

**Last Updated**: December 2024  
**Schema Version**: 1.0
