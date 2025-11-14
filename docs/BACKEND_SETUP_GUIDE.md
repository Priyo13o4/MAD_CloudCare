# CloudCare Backend Setup Guide

**Version:** 1.0  
**Last Updated:** November 2025

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture Design](#architecture-design)
3. [Technology Stack](#technology-stack)
4. [Database Design](#database-design)
5. [Aadhar-Based UID System](#aadhar-based-uid-system)
6. [API Endpoints](#api-endpoints)
7. [Installation & Setup](#installation--setup)
8. [Wearables Integration](#wearables-integration)
9. [Security Implementation](#security-implementation)
10. [Deployment](#deployment)

---

## Overview

This guide provides a complete blueprint for setting up the CloudCare backend infrastructure with a focus on:

- **Aadhar-based Patient UID**: Using Aadhar numbers as the foundation for unique patient identification
- **Dual Database Architecture**: MongoDB for wearables/health data, PostgreSQL for authentication/structured data
- **Document Request System**: Enable fetching medical records from hospitals not yet uploaded to the cloud
- **Real-time Health Monitoring**: WebSocket support for live wearable data
- **Scalable Microservices**: Independent services for different functionalities

---

## Architecture Design

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Android App                          │
│              (Patient/Doctor/Hospital)                  │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ HTTPS/REST API
                     │ WebSocket (real-time)
                     │
┌────────────────────▼────────────────────────────────────┐
│                API Gateway (NGINX)                       │
│             - Load Balancing                            │
│             - SSL Termination                           │
│             - Rate Limiting                             │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
┌───────▼──────┐ ┌──▼─────────┐ ┌▼──────────────┐
│   Auth       │ │  Wearables │ │   Medical     │
│   Service    │ │  Service   │ │   Records     │
│  (Flask)     │ │  (Flask)   │ │   Service     │
│              │ │            │ │   (Flask)     │
└───────┬──────┘ └──┬─────────┘ └┬──────────────┘
        │            │             │
        │            │             │
┌───────▼──────┐ ┌──▼─────────┐ ┌▼──────────────┐
│  PostgreSQL  │ │  MongoDB   │ │   MongoDB     │
│              │ │            │ │               │
│ - Users      │ │ - Health   │ │ - Records     │
│ - Auth       │ │   Metrics  │ │ - Documents   │
│ - Consents   │ │ - Devices  │ │ - Images      │
│ - Facilities │ │ - Alerts   │ │               │
└──────────────┘ └────────────┘ └───────────────┘
        │
        │
┌───────▼──────────────────────────────────────────┐
│           Redis Cache                            │
│  - Session Management                            │
│  - Rate Limiting                                 │
│  - Real-time Data Buffer                         │
└──────────────────────────────────────────────────┘
```

### Microservices Breakdown

1. **Auth Service**: User authentication, authorization, Aadhar verification
2. **Wearables Service**: Health data ingestion from wearables, real-time monitoring
3. **Medical Records Service**: Document management, sharing, consent handling
4. **Consent Service**: Manage data access permissions
5. **Notification Service**: Push notifications, alerts
6. **Document Request Service**: Request records from hospitals (new feature)

---

## Technology Stack

### Backend Framework
- **Primary**: Flask (Python 3.11+)
- **Alternative**: FastAPI (for high-performance endpoints)
- **API Standard**: RESTful with OpenAPI/Swagger documentation

### Databases

#### PostgreSQL (Relational Data)
**Version**: 15+  
**Use Cases**:
- User authentication (doctors, hospitals, staff)
- Patient metadata (linked to Aadhar)
- Consent records
- Facility information
- Appointments
- Audit logs

**Why PostgreSQL?**
- ACID compliance for critical data
- Strong relational integrity
- Excellent for structured data
- Advanced querying capabilities
- JSON support for flexible fields

#### MongoDB (Document Store)
**Version**: 6.0+  
**Use Cases**:
- Wearable device data (time-series)
- Health metrics (heart rate, steps, etc.)
- Medical records (PDFs, images)
- Device configurations
- Real-time alerts

**Why MongoDB?**
- Flexible schema for diverse health metrics
- Excellent for time-series data
- High write throughput for streaming data
- Horizontal scalability
- GridFS for large file storage

### Additional Components

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Cache | Redis 7+ | Session management, rate limiting |
| Message Queue | RabbitMQ / Redis | Async task processing |
| File Storage | MinIO / AWS S3 | Medical documents, images |
| Real-time | Socket.IO | Live health monitoring |
| Task Queue | Celery | Background jobs |
| API Gateway | NGINX | Load balancing, SSL |
| Monitoring | Prometheus + Grafana | System metrics |
| Logging | ELK Stack | Centralized logging |

---

## Database Design

### PostgreSQL Schema

#### Users Table (Authentication)
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    aadhar_hash VARCHAR(128) UNIQUE NOT NULL,  -- Hashed Aadhar
    user_type VARCHAR(20) NOT NULL,            -- 'patient', 'doctor', 'hospital'
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(15),
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    two_factor_enabled BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_users_aadhar ON users(aadhar_hash);
CREATE INDEX idx_users_email ON users(email);
```

#### Patients Table
```sql
CREATE TABLE patients (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    aadhar_uid VARCHAR(64) UNIQUE NOT NULL,    -- Aadhar-derived UID
    aadhar_number_encrypted BYTEA NOT NULL,     -- Encrypted Aadhar
    full_name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20),
    blood_type VARCHAR(5),
    address TEXT,
    emergency_contact VARCHAR(255),
    emergency_phone VARCHAR(15),
    insurance_provider VARCHAR(255),
    insurance_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_patients_aadhar_uid ON patients(aadhar_uid);
```

#### Doctors Table
```sql
CREATE TABLE doctors (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    registration_number VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    specialization VARCHAR(100),
    qualification VARCHAR(255),
    hospital_id INTEGER REFERENCES hospitals(id),
    department VARCHAR(100),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(15),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_verified BOOLEAN DEFAULT FALSE
);
```

#### Hospitals Table
```sql
CREATE TABLE hospitals (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    hospital_name VARCHAR(255) NOT NULL,
    registration_number VARCHAR(100) UNIQUE NOT NULL,
    type VARCHAR(50),  -- 'hospital', 'clinic', 'lab', 'pharmacy'
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(15),
    license_number VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_verified BOOLEAN DEFAULT FALSE
);
```

#### Consents Table
```sql
CREATE TABLE consents (
    id SERIAL PRIMARY KEY,
    patient_aadhar_uid VARCHAR(64) REFERENCES patients(aadhar_uid),
    facility_id INTEGER REFERENCES hospitals(id),
    doctor_id INTEGER REFERENCES doctors(id),
    consent_type VARCHAR(50) NOT NULL,  -- 'read', 'write', 'share'
    data_categories TEXT[],  -- Array: ['wearables', 'lab_reports', 'prescriptions']
    granted_at TIMESTAMP,
    expires_at TIMESTAMP,
    revoked_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'pending',  -- 'pending', 'approved', 'denied', 'revoked'
    purpose TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_consents_patient ON consents(patient_aadhar_uid);
CREATE INDEX idx_consents_facility ON consents(facility_id);
CREATE INDEX idx_consents_status ON consents(status);
```

#### Patient-Facility Links Table
```sql
CREATE TABLE patient_facility_links (
    id SERIAL PRIMARY KEY,
    patient_aadhar_uid VARCHAR(64) REFERENCES patients(aadhar_uid),
    facility_id INTEGER REFERENCES hospitals(id),
    facility_patient_id VARCHAR(100),  -- Hospital's internal ID for patient
    linked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_visit TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    UNIQUE(patient_aadhar_uid, facility_id)
);
```

#### Document Request Table (NEW)
```sql
CREATE TABLE document_requests (
    id SERIAL PRIMARY KEY,
    patient_aadhar_uid VARCHAR(64) REFERENCES patients(aadhar_uid),
    requesting_facility_id INTEGER REFERENCES hospitals(id),
    source_facility_id INTEGER REFERENCES hospitals(id),  -- Where document exists
    document_type VARCHAR(50),  -- 'lab_report', 'prescription', 'imaging', etc.
    request_date DATE,
    urgency VARCHAR(20) DEFAULT 'normal',  -- 'urgent', 'normal', 'low'
    status VARCHAR(30) DEFAULT 'pending',  -- 'pending', 'approved', 'fulfilled', 'denied'
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fulfilled_at TIMESTAMP,
    document_id VARCHAR(100)  -- MongoDB document ID after upload
);

CREATE INDEX idx_doc_req_patient ON document_requests(patient_aadhar_uid);
CREATE INDEX idx_doc_req_status ON document_requests(status);
```

#### Audit Logs Table
```sql
CREATE TABLE audit_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    patient_aadhar_uid VARCHAR(64),
    action VARCHAR(100) NOT NULL,  -- 'consent_granted', 'data_accessed', etc.
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    ip_address INET,
    user_agent TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details JSONB
);

CREATE INDEX idx_audit_patient ON audit_logs(patient_aadhar_uid);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
```

### MongoDB Collections

#### Health Metrics Collection
```javascript
{
  "_id": ObjectId("..."),
  "patient_aadhar_uid": "AADHAR_DERIVED_UID_HERE",
  "device_id": "xiaomi_mi_band_12345",
  "device_type": "fitness_tracker",
  "metric_type": "heart_rate",  // or "steps", "sleep", "calories", "blood_pressure"
  "value": 75,
  "unit": "bpm",
  "timestamp": ISODate("2025-11-14T10:30:00Z"),
  "location": {
    "type": "Point",
    "coordinates": [72.8777, 19.0760]  // Optional GPS
  },
  "metadata": {
    "activity": "walking",
    "confidence": 0.95
  },
  "created_at": ISODate("2025-11-14T10:30:05Z")
}

// Indexes
db.health_metrics.createIndex({ "patient_aadhar_uid": 1, "timestamp": -1 })
db.health_metrics.createIndex({ "metric_type": 1, "timestamp": -1 })
db.health_metrics.createIndex({ "device_id": 1 })
```

#### Wearable Devices Collection
```javascript
{
  "_id": ObjectId("..."),
  "patient_aadhar_uid": "AADHAR_DERIVED_UID_HERE",
  "device_id": "xiaomi_mi_band_12345",
  "device_name": "Xiaomi Mi Band 7",
  "device_type": "fitness_tracker",
  "manufacturer": "Xiaomi",
  "model": "Mi Band 7",
  "paired_at": ISODate("2025-11-01T08:00:00Z"),
  "last_sync": ISODate("2025-11-14T10:30:00Z"),
  "battery_level": 85,
  "firmware_version": "2.3.1",
  "sync_interval": 300,  // seconds
  "is_active": true,
  "capabilities": ["heart_rate", "steps", "sleep", "calories"],
  "settings": {
    "heart_rate_continuous": true,
    "alert_threshold": 120
  }
}
```

#### Health Alerts Collection
```javascript
{
  "_id": ObjectId("..."),
  "alert_id": "ALERT_2025_001",
  "patient_aadhar_uid": "AADHAR_DERIVED_UID_HERE",
  "alert_type": "heart_rate_abnormal",
  "severity": "high",  // "critical", "high", "medium", "low"
  "triggered_at": ISODate("2025-11-14T10:32:00Z"),
  "resolved_at": null,
  "metric_data": {
    "metric_type": "heart_rate",
    "value": 145,
    "threshold": 120,
    "unit": "bpm"
  },
  "notified_doctors": ["DOC_12345"],
  "patient_status": "monitoring",
  "notes": [],
  "is_active": true
}
```

#### Medical Documents Collection
```javascript
{
  "_id": ObjectId("..."),
  "patient_aadhar_uid": "AADHAR_DERIVED_UID_HERE",
  "document_type": "lab_report",
  "document_title": "Blood Test Results",
  "facility_id": 123,
  "facility_name": "Dr Lal PathLabs",
  "doctor_id": 456,
  "doctor_name": "Dr. Sharma",
  "report_date": ISODate("2025-11-10T00:00:00Z"),
  "upload_date": ISODate("2025-11-11T14:20:00Z"),
  "file_storage": {
    "storage_type": "s3",  // or "gridfs", "local"
    "file_path": "s3://cloudcare-docs/reports/...",
    "file_size": 2048576,
    "mime_type": "application/pdf",
    "checksum": "sha256:..."
  },
  "metadata": {
    "test_type": "complete_blood_count",
    "tags": ["routine", "annual_checkup"]
  },
  "access_log": [
    {
      "accessed_by": "DOC_789",
      "access_type": "view",
      "timestamp": ISODate("2025-11-12T09:00:00Z")
    }
  ],
  "is_sensitive": false,
  "consent_required": true
}
```

---

## Aadhar-Based UID System

### Why Aadhar-Based UIDs?

**Benefits:**
1. **Universal Identification**: Works across all hospitals/facilities in India
2. **Prevents Duplication**: One unique ID per person nationwide
3. **Document Portability**: Easy to request records from any hospital using Aadhar
4. **Future-Ready**: Enables integration with ABDM (Ayushman Bharat Digital Mission)
5. **Privacy-Preserving**: We don't store raw Aadhar numbers

### Implementation Strategy

#### 1. UID Generation Algorithm

```python
import hashlib
import hmac
import base64
from cryptography.fernet import Fernet

class AadharUIDGenerator:
    """
    Generate patient UID from Aadhar number
    - Uses HMAC-SHA256 for irreversible hashing
    - Adds application-specific salt
    - Results in consistent UID for same Aadhar
    """
    
    def __init__(self, secret_key: str):
        """
        Args:
            secret_key: Application secret (stored in environment)
        """
        self.secret_key = secret_key.encode()
        
    def generate_uid(self, aadhar_number: str) -> str:
        """
        Generate UID from Aadhar number
        
        Args:
            aadhar_number: 12-digit Aadhar number (string)
            
        Returns:
            64-character hex string UID
        """
        # Validate Aadhar format
        if not self.validate_aadhar(aadhar_number):
            raise ValueError("Invalid Aadhar number format")
        
        # Clean Aadhar number (remove spaces)
        clean_aadhar = aadhar_number.replace(" ", "").replace("-", "")
        
        # Generate HMAC-SHA256 hash
        uid_hash = hmac.new(
            self.secret_key,
            clean_aadhar.encode(),
            hashlib.sha256
        ).hexdigest()
        
        return uid_hash
    
    @staticmethod
    def validate_aadhar(aadhar: str) -> bool:
        """Validate Aadhar number format"""
        clean = aadhar.replace(" ", "").replace("-", "")
        return len(clean) == 12 and clean.isdigit()
    
    def encrypt_aadhar(self, aadhar_number: str, encryption_key: bytes) -> bytes:
        """
        Encrypt Aadhar for secure storage
        Only stored encrypted, never in plain text
        """
        fernet = Fernet(encryption_key)
        return fernet.encrypt(aadhar_number.encode())
    
    def decrypt_aadhar(self, encrypted_aadhar: bytes, encryption_key: bytes) -> str:
        """Decrypt Aadhar (only for verification purposes)"""
        fernet = Fernet(encryption_key)
        return fernet.decrypt(encrypted_aadhar).decode()
```

#### 2. Patient Registration Flow

```python
from flask import Flask, request, jsonify
from werkzeug.security import generate_password_hash
import os

app = Flask(__name__)

# Initialize UID generator with secret key
uid_generator = AadharUIDGenerator(os.getenv('AADHAR_SECRET_KEY'))
encryption_key = os.getenv('AADHAR_ENCRYPTION_KEY').encode()

@app.route('/api/v1/patient/register', methods=['POST'])
def register_patient():
    """
    Patient registration endpoint
    
    Request Body:
    {
        "aadhar_number": "1234 5678 9012",
        "full_name": "Rajesh Kumar",
        "date_of_birth": "1990-05-15",
        "gender": "male",
        "phone": "+91-9876543210",
        "email": "rajesh@example.com",
        "password": "SecurePassword123"
    }
    """
    data = request.get_json()
    
    # Validate required fields
    required_fields = ['aadhar_number', 'full_name', 'date_of_birth', 'password']
    if not all(field in data for field in required_fields):
        return jsonify({"error": "Missing required fields"}), 400
    
    try:
        # Generate Aadhar-based UID
        aadhar_uid = uid_generator.generate_uid(data['aadhar_number'])
        
        # Check if patient already exists
        existing_patient = db.patients.find_one({"aadhar_uid": aadhar_uid})
        if existing_patient:
            return jsonify({"error": "Patient already registered"}), 409
        
        # Encrypt Aadhar for storage
        encrypted_aadhar = uid_generator.encrypt_aadhar(
            data['aadhar_number'], 
            encryption_key
        )
        
        # Create user record (PostgreSQL)
        user_id = create_user(
            aadhar_hash=hashlib.sha256(aadhar_uid.encode()).hexdigest(),
            user_type='patient',
            email=data.get('email'),
            phone=data.get('phone'),
            password_hash=generate_password_hash(data['password'])
        )
        
        # Create patient record (PostgreSQL)
        patient_id = create_patient(
            user_id=user_id,
            aadhar_uid=aadhar_uid,
            aadhar_encrypted=encrypted_aadhar,
            full_name=data['full_name'],
            date_of_birth=data['date_of_birth'],
            gender=data.get('gender'),
            phone=data.get('phone')
        )
        
        # Log registration
        log_audit(
            user_id=user_id,
            action='patient_registered',
            details={'aadhar_uid': aadhar_uid}
        )
        
        return jsonify({
            "status": "success",
            "message": "Patient registered successfully",
            "patient_uid": aadhar_uid,
            "patient_id": patient_id
        }), 201
        
    except ValueError as e:
        return jsonify({"error": str(e)}), 400
    except Exception as e:
        app.logger.error(f"Registration error: {str(e)}")
        return jsonify({"error": "Internal server error"}), 500
```

#### 3. Document Request System (Cross-Hospital)

```python
@app.route('/api/v1/documents/request', methods=['POST'])
def request_document():
    """
    Request medical document from another hospital
    
    This enables patients to request their records from hospitals
    that haven't uploaded them to CloudCare yet
    
    Request Body:
    {
        "patient_aadhar_uid": "generated_uid_here",
        "source_hospital_id": 123,
        "document_type": "lab_report",
        "visit_date": "2025-10-15",
        "urgency": "normal",
        "notes": "Required for second opinion"
    }
    """
    data = request.get_json()
    
    # Verify patient identity
    patient = verify_patient_token(request.headers.get('Authorization'))
    if not patient or patient['aadhar_uid'] != data['patient_aadhar_uid']:
        return jsonify({"error": "Unauthorized"}), 401
    
    # Check if hospital exists
    hospital = db.hospitals.find_one({"id": data['source_hospital_id']})
    if not hospital:
        return jsonify({"error": "Hospital not found"}), 404
    
    # Create document request
    request_id = create_document_request(
        patient_aadhar_uid=data['patient_aadhar_uid'],
        requesting_facility_id=data.get('current_facility_id'),
        source_facility_id=data['source_hospital_id'],
        document_type=data['document_type'],
        request_date=data.get('visit_date'),
        urgency=data.get('urgency', 'normal'),
        notes=data.get('notes')
    )
    
    # Send notification to source hospital
    notify_hospital(
        hospital_id=data['source_hospital_id'],
        notification_type='document_request',
        message=f"Document request from patient (UID: {data['patient_aadhar_uid'][:8]}...)",
        request_id=request_id
    )
    
    # Send email to hospital
    send_email(
        to=hospital['contact_email'],
        subject="CloudCare: Medical Document Request",
        body=f"""
        A patient has requested their medical records through CloudCare.
        
        Patient UID: {data['patient_aadhar_uid']}
        Document Type: {data['document_type']}
        Visit Date: {data.get('visit_date')}
        
        Please log in to CloudCare portal to process this request.
        """
    )
    
    return jsonify({
        "status": "success",
        "message": "Document request submitted",
        "request_id": request_id,
        "expected_response_time": "3-5 business days"
    }), 201


@app.route('/api/v1/hospital/document-requests', methods=['GET'])
def get_hospital_document_requests():
    """
    Hospital endpoint to view pending document requests
    """
    hospital = verify_hospital_token(request.headers.get('Authorization'))
    
    # Get pending requests for this hospital
    requests = db.document_requests.find({
        "source_facility_id": hospital['id'],
        "status": "pending"
    }).sort("created_at", -1)
    
    return jsonify({
        "status": "success",
        "pending_requests": list(requests),
        "count": requests.count()
    }), 200


@app.route('/api/v1/hospital/document-requests/<request_id>/fulfill', methods=['POST'])
def fulfill_document_request(request_id):
    """
    Hospital fulfills document request by uploading the document
    """
    hospital = verify_hospital_token(request.headers.get('Authorization'))
    
    # Get request details
    doc_request = db.document_requests.find_one({"id": request_id})
    if not doc_request:
        return jsonify({"error": "Request not found"}), 404
    
    # Verify hospital is authorized
    if doc_request['source_facility_id'] != hospital['id']:
        return jsonify({"error": "Unauthorized"}), 403
    
    # Handle file upload
    if 'file' not in request.files:
        return jsonify({"error": "No file provided"}), 400
    
    file = request.files['file']
    
    # Upload to S3/MinIO
    document_url = upload_medical_document(
        file=file,
        patient_uid=doc_request['patient_aadhar_uid'],
        document_type=doc_request['document_type']
    )
    
    # Create medical document record in MongoDB
    document_id = create_medical_document(
        patient_aadhar_uid=doc_request['patient_aadhar_uid'],
        document_type=doc_request['document_type'],
        facility_id=hospital['id'],
        file_url=document_url,
        metadata=request.form.get('metadata', {})
    )
    
    # Update request status
    update_document_request(
        request_id=request_id,
        status='fulfilled',
        document_id=document_id
    )
    
    # Notify patient
    notify_patient(
        patient_uid=doc_request['patient_aadhar_uid'],
        notification_type='document_received',
        message=f"Your requested document has been uploaded by {hospital['name']}"
    )
    
    return jsonify({
        "status": "success",
        "message": "Document uploaded successfully",
        "document_id": document_id
    }), 200
```

### UID Usage Examples

#### Finding Patient Records Across Facilities
```python
def get_patient_complete_history(aadhar_uid: str):
    """
    Fetch complete medical history across all facilities
    """
    # Get all facilities linked to patient
    linked_facilities = db.patient_facility_links.find({
        "patient_aadhar_uid": aadhar_uid,
        "is_active": True
    })
    
    # Get all documents from MongoDB
    all_documents = db.medical_documents.find({
        "patient_aadhar_uid": aadhar_uid
    }).sort("report_date", -1)
    
    # Get health metrics
    health_data = db.health_metrics.find({
        "patient_aadhar_uid": aadhar_uid
    }).sort("timestamp", -1).limit(1000)
    
    return {
        "linked_facilities": list(linked_facilities),
        "documents": list(all_documents),
        "health_metrics": list(health_data)
    }
```

---

## API Endpoints

### Authentication Endpoints

#### POST `/api/v1/auth/register`
Register new user (patient/doctor/hospital)

#### POST `/api/v1/auth/login`
Login with credentials

#### POST `/api/v1/auth/logout`
Logout and invalidate token

#### POST `/api/v1/auth/refresh`
Refresh JWT token

#### POST `/api/v1/auth/verify-aadhar`
Verify Aadhar via OTP (integration with UIDAI)

### Patient Endpoints

#### GET `/api/v1/patient/profile`
Get patient profile

#### PUT `/api/v1/patient/profile`
Update patient profile

#### GET `/api/v1/patient/health-summary`
Get health summary (steps, heart rate, etc.)

#### GET `/api/v1/patient/facilities`
Get linked healthcare facilities

#### POST `/api/v1/patient/link-facility`
Link to a new facility

### Wearables Endpoints

#### POST `/api/v1/wearables/device/register`
Register new wearable device

#### POST `/api/v1/wearables/data/sync`
Sync health data from wearable

```python
@app.route('/api/v1/wearables/data/sync', methods=['POST'])
def sync_wearable_data():
    """
    Sync health data from wearable device
    
    Request Body:
    {
        "patient_aadhar_uid": "uid_here",
        "device_id": "xiaomi_12345",
        "data_points": [
            {
                "metric_type": "heart_rate",
                "value": 75,
                "unit": "bpm",
                "timestamp": "2025-11-14T10:30:00Z"
            },
            {
                "metric_type": "steps",
                "value": 1250,
                "unit": "steps",
                "timestamp": "2025-11-14T10:30:00Z"
            }
        ]
    }
    """
    data = request.get_json()
    
    # Verify device belongs to patient
    device = mongo_db.wearable_devices.find_one({
        "device_id": data['device_id'],
        "patient_aadhar_uid": data['patient_aadhar_uid']
    })
    
    if not device:
        return jsonify({"error": "Device not found or unauthorized"}), 404
    
    # Insert health metrics
    inserted_ids = []
    for data_point in data['data_points']:
        metric = {
            "patient_aadhar_uid": data['patient_aadhar_uid'],
            "device_id": data['device_id'],
            "device_type": device['device_type'],
            "metric_type": data_point['metric_type'],
            "value": data_point['value'],
            "unit": data_point['unit'],
            "timestamp": data_point['timestamp'],
            "created_at": datetime.utcnow()
        }
        
        result = mongo_db.health_metrics.insert_one(metric)
        inserted_ids.append(str(result.inserted_id))
        
        # Check for alerts
        check_health_alerts(data['patient_aadhar_uid'], data_point)
    
    # Update device last sync
    mongo_db.wearable_devices.update_one(
        {"device_id": data['device_id']},
        {
            "$set": {
                "last_sync": datetime.utcnow(),
                "battery_level": data.get('battery_level')
            }
        }
    )
    
    return jsonify({
        "status": "success",
        "synced_count": len(inserted_ids),
        "inserted_ids": inserted_ids
    }), 201
```

#### GET `/api/v1/wearables/data/history`
Get historical health data

#### GET `/api/v1/wearables/devices`
List all devices for patient

#### DELETE `/api/v1/wearables/device/<device_id>`
Remove wearable device

### Consent Endpoints

#### GET `/api/v1/consents`
Get all consents for patient

#### POST `/api/v1/consents/request`
Request consent from patient (hospital/doctor)

#### PUT `/api/v1/consents/<consent_id>/approve`
Approve consent request

#### PUT `/api/v1/consents/<consent_id>/deny`
Deny consent request

#### PUT `/api/v1/consents/<consent_id>/revoke`
Revoke existing consent

### Medical Records Endpoints

#### GET `/api/v1/records`
Get medical records (with consent check)

#### POST `/api/v1/records/upload`
Upload new medical record

#### GET `/api/v1/records/<record_id>`
Get specific record

#### DELETE `/api/v1/records/<record_id>`
Delete record

### Document Request Endpoints

#### POST `/api/v1/documents/request`
Request document from hospital

#### GET `/api/v1/documents/requests`
Get document request status

#### GET `/api/v1/hospital/document-requests`
Hospital: View pending requests

#### POST `/api/v1/hospital/document-requests/<id>/fulfill`
Hospital: Upload requested document

---

## Installation & Setup

### Prerequisites

- Python 3.11+
- PostgreSQL 15+
- MongoDB 6.0+
- Redis 7+
- NGINX (for production)
- SSL Certificate (Let's Encrypt)

### Development Environment Setup

#### 1. Clone Repository
```bash
git clone https://github.com/yourorg/cloudcare-backend.git
cd cloudcare-backend
```

#### 2. Create Virtual Environment
```bash
python3 -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

#### 3. Install Dependencies
```bash
pip install -r requirements.txt
```

**requirements.txt:**
```txt
Flask==3.0.0
Flask-CORS==4.0.0
Flask-JWT-Extended==4.5.3
Flask-SocketIO==5.3.5
psycopg2-binary==2.9.9
pymongo==4.6.0
redis==5.0.1
celery==5.3.4
cryptography==41.0.7
python-dotenv==1.0.0
gunicorn==21.2.0
requests==2.31.0
Pillow==10.1.0
boto3==1.34.0  # For S3
sendgrid==6.11.0  # For emails
python-socketio==5.10.0
eventlet==0.33.3
```

#### 4. Environment Configuration

Create `.env` file:
```bash
# Application
FLASK_ENV=development
SECRET_KEY=your_super_secret_key_here
AADHAR_SECRET_KEY=aadhar_hashing_secret
AADHAR_ENCRYPTION_KEY=base64_encoded_fernet_key

# PostgreSQL
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=cloudcare
POSTGRES_USER=cloudcare_user
POSTGRES_PASSWORD=secure_password

# MongoDB
MONGODB_URI=mongodb://localhost:27017/cloudcare
MONGODB_HOST=localhost
MONGODB_PORT=27017
MONGODB_DB=cloudcare

# Redis
REDIS_URL=redis://localhost:6379/0

# JWT
JWT_SECRET_KEY=jwt_secret_key_here
JWT_ACCESS_TOKEN_EXPIRES=3600  # 1 hour
JWT_REFRESH_TOKEN_EXPIRES=2592000  # 30 days

# File Storage (S3/MinIO)
S3_BUCKET=cloudcare-documents
S3_REGION=ap-south-1
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key

# Email (SendGrid)
SENDGRID_API_KEY=your_sendgrid_key
FROM_EMAIL=noreply@cloudcare.com

# UIDAI (Aadhar Verification) - Mock for development
UIDAI_API_ENABLED=false
UIDAI_API_URL=https://api.uidai.gov.in
UIDAI_API_KEY=your_uidai_key
```

#### 5. Database Setup

**PostgreSQL:**
```bash
# Create database
psql -U postgres
CREATE DATABASE cloudcare;
CREATE USER cloudcare_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE cloudcare TO cloudcare_user;

# Run migrations
python manage.py db init
python manage.py db migrate
python manage.py db upgrade
```

**MongoDB:**
```bash
# MongoDB will auto-create database
# Create indexes via Python script
python scripts/create_mongo_indexes.py
```

#### 6. Start Services

```bash
# Start Flask development server
python app.py

# Start Celery worker (separate terminal)
celery -A app.celery worker --loglevel=info

# Start Celery beat (scheduler)
celery -A app.celery beat --loglevel=info
```

### Production Deployment

#### 1. NGINX Configuration

```nginx
# /etc/nginx/sites-available/cloudcare

upstream cloudcare_app {
    server 127.0.0.1:5000;
    server 127.0.0.1:5001;
    server 127.0.0.1:5002;
}

server {
    listen 80;
    server_name api.cloudcare.com;
    
    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.cloudcare.com;
    
    # SSL Configuration
    ssl_certificate /etc/letsencrypt/live/api.cloudcare.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.cloudcare.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers on;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512;
    
    # Security Headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # Rate Limiting
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;
    limit_req zone=api_limit burst=20 nodelay;
    
    # Max upload size
    client_max_body_size 50M;
    
    location / {
        proxy_pass http://cloudcare_app;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }
    
    # WebSocket support
    location /socket.io {
        proxy_pass http://cloudcare_app;
        proxy_http_version 1.1;
        proxy_buffering off;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "Upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

#### 2. Systemd Service

```ini
# /etc/systemd/system/cloudcare.service

[Unit]
Description=CloudCare Flask Application
After=network.target postgresql.service mongodb.service redis.service

[Service]
Type=notify
User=cloudcare
Group=cloudcare
WorkingDirectory=/opt/cloudcare
Environment="PATH=/opt/cloudcare/venv/bin"
ExecStart=/opt/cloudcare/venv/bin/gunicorn \
    --workers 4 \
    --worker-class eventlet \
    --bind 0.0.0.0:5000 \
    --timeout 120 \
    --access-logfile /var/log/cloudcare/access.log \
    --error-logfile /var/log/cloudcare/error.log \
    app:app

Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Start service:
```bash
sudo systemctl daemon-reload
sudo systemctl enable cloudcare
sudo systemctl start cloudcare
```

---

## Wearables Integration

### Supported Devices

1. **Xiaomi Mi Band** (tested)
2. **Fitbit**
3. **Apple Watch** (via HealthKit)
4. **Google Fit devices**
5. **Samsung Galaxy Watch**

### Integration Methods

#### 1. Direct SDK Integration (Mobile App)

```kotlin
// Android - Google Fit API
class GoogleFitManager(private val context: Context) {
    
    suspend fun syncHealthData(patientUid: String) {
        // Get fitness data from Google Fit
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_HEART_RATE_BPM)
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED)
            .addDataType(DataType.TYPE_SLEEP_SEGMENT)
            .build()
        
        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
        
        // Read today's data
        val endTime = LocalDateTime.now()
        val startTime = endTime.minusDays(1)
        
        val request = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
            .aggregate(DataType.TYPE_HEART_RATE_BPM)
            .bucketByTime(1, TimeUnit.HOURS)
            .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
            .build()
        
        val response = Fitness.getHistoryClient(context, account)
            .readData(request)
            .await()
        
        // Parse and sync to backend
        val dataPoints = parseHealthData(response)
        syncToBackend(patientUid, dataPoints)
    }
    
    private suspend fun syncToBackend(patientUid: String, dataPoints: List<HealthDataPoint>) {
        val apiService = RetrofitClient.wearablesService
        
        val syncRequest = WearableDataSyncRequest(
            patientAadharUid = patientUid,
            deviceId = "google_fit_${Build.ID}",
            dataPoints = dataPoints.map {
                DataPointDto(
                    metricType = it.type,
                    value = it.value,
                    unit = it.unit,
                    timestamp = it.timestamp.toString()
                )
            }
        )
        
        apiService.syncData(syncRequest)
    }
}
```

#### 2. Webhook Integration (For Cloud-Synced Devices)

```python
@app.route('/api/v1/webhooks/fitbit', methods=['POST'])
def fitbit_webhook():
    """
    Receive health data from Fitbit webhook
    
    Fitbit will push data updates to this endpoint
    """
    # Verify Fitbit signature
    if not verify_fitbit_signature(request):
        return jsonify({"error": "Invalid signature"}), 401
    
    data = request.get_json()
    
    # Process each user's data
    for user_update in data:
        # Get patient UID from Fitbit user ID mapping
        patient_uid = get_patient_uid_from_fitbit_user(user_update['owner_id'])
        
        # Fetch detailed data from Fitbit API
        fitbit_data = fetch_fitbit_user_data(
            user_update['owner_id'],
            user_update['date']
        )
        
        # Transform and store in MongoDB
        store_health_metrics(patient_uid, fitbit_data)
    
    return jsonify({"status": "success"}), 200
```

#### 3. Background Sync Service

```python
# Celery task for periodic sync
from celery import Celery

celery = Celery('cloudcare')

@celery.task
def sync_all_wearable_devices():
    """
    Periodic task to sync data from all connected devices
    Runs every 15 minutes
    """
    # Get all active devices
    devices = mongo_db.wearable_devices.find({"is_active": True})
    
    for device in devices:
        try:
            if device['device_type'] == 'fitbit':
                sync_fitbit_device(device)
            elif device['device_type'] == 'google_fit':
                sync_google_fit_device(device)
            elif device['device_type'] == 'xiaomi':
                sync_xiaomi_device(device)
        except Exception as e:
            logger.error(f"Sync failed for device {device['device_id']}: {str(e)}")
            # Continue with next device

@celery.beat_schedule = {
    'sync-wearables': {
        'task': 'tasks.sync_all_wearable_devices',
        'schedule': crontab(minute='*/15'),  # Every 15 minutes
    }
}
```

### Real-time Monitoring (WebSocket)

```python
from flask_socketio import SocketIO, emit, join_room

socketio = SocketIO(app, cors_allowed_origins="*")

@socketio.on('connect')
def handle_connect():
    """Client connects to WebSocket"""
    print('Client connected')

@socketio.on('subscribe_patient')
def subscribe_to_patient(data):
    """
    Doctor subscribes to patient's real-time health data
    """
    patient_uid = data['patient_aadhar_uid']
    doctor_id = data['doctor_id']
    
    # Verify doctor has consent
    if verify_doctor_consent(doctor_id, patient_uid):
        room = f"patient_{patient_uid}"
        join_room(room)
        emit('subscribed', {'patient_uid': patient_uid})
    else:
        emit('error', {'message': 'No consent to view this patient'})

def broadcast_health_alert(patient_uid, alert_data):
    """
    Broadcast health alert to all subscribed doctors
    Called when critical metric is detected
    """
    room = f"patient_{patient_uid}"
    socketio.emit('health_alert', alert_data, room=room)
```

---

## Security Implementation

### 1. Data Encryption

**At Rest:**
```python
from cryptography.fernet import Fernet
import os

class DataEncryption:
    def __init__(self):
        self.key = os.getenv('ENCRYPTION_KEY').encode()
        self.fernet = Fernet(self.key)
    
    def encrypt(self, data: str) -> bytes:
        return self.fernet.encrypt(data.encode())
    
    def decrypt(self, encrypted_data: bytes) -> str:
        return self.fernet.decrypt(encrypted_data).decode()
```

**In Transit:**
- All API calls over HTTPS/TLS 1.2+
- Certificate pinning in mobile app

### 2. JWT Authentication

```python
from flask_jwt_extended import create_access_token, jwt_required, get_jwt_identity

@app.route('/api/v1/auth/login', methods=['POST'])
def login():
    data = request.get_json()
    
    # Validate credentials
    user = authenticate_user(data['email'], data['password'])
    
    if user:
        # Create JWT token
        access_token = create_access_token(
            identity=user['id'],
            additional_claims={
                'user_type': user['user_type'],
                'aadhar_uid': user.get('aadhar_uid')
            }
        )
        
        return jsonify({
            "access_token": access_token,
            "user_type": user['user_type']
        }), 200
    else:
        return jsonify({"error": "Invalid credentials"}), 401

@app.route('/api/v1/patient/profile', methods=['GET'])
@jwt_required()
def get_profile():
    current_user_id = get_jwt_identity()
    # ... fetch and return profile
```

### 3. Rate Limiting

```python
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

limiter = Limiter(
    app=app,
    key_func=get_remote_address,
    storage_uri="redis://localhost:6379"
)

@app.route('/api/v1/auth/login', methods=['POST'])
@limiter.limit("5 per minute")  # Max 5 login attempts per minute
def login():
    # ... login logic
```

### 4. RBAC (Role-Based Access Control)

```python
from functools import wraps
from flask_jwt_extended import get_jwt

def require_role(*allowed_roles):
    """Decorator to check user role"""
    def wrapper(fn):
        @wraps(fn)
        @jwt_required()
        def decorator(*args, **kwargs):
            claims = get_jwt()
            if claims.get('user_type') not in allowed_roles:
                return jsonify({"error": "Insufficient permissions"}), 403
            return fn(*args, **kwargs)
        return decorator
    return wrapper

@app.route('/api/v1/hospital/dashboard', methods=['GET'])
@require_role('hospital', 'admin')
def hospital_dashboard():
    # Only hospitals and admins can access
    pass
```

### 5. Consent Verification Middleware

```python
def verify_data_access(patient_uid: str, requester_id: int, requester_type: str) -> bool:
    """
    Verify if requester has consent to access patient data
    """
    # Patient can always access their own data
    if requester_type == 'patient':
        patient = get_patient_by_user_id(requester_id)
        return patient['aadhar_uid'] == patient_uid
    
    # Check active consent
    consent = db.consents.find_one({
        "patient_aadhar_uid": patient_uid,
        f"{requester_type}_id": requester_id,
        "status": "approved",
        "expires_at": {"$gt": datetime.utcnow()}
    })
    
    return consent is not None
```

---

## Deployment

### Docker Deployment

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: cloudcare
      POSTGRES_USER: cloudcare_user
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    restart: unless-stopped

  mongodb:
    image: mongo:6.0
    environment:
      MONGO_INITDB_DATABASE: cloudcare
    volumes:
      - mongo_data:/data/db
    ports:
      - "27017:27017"
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    restart: unless-stopped

  app:
    build: .
    command: gunicorn --workers 4 --worker-class eventlet --bind 0.0.0.0:5000 app:app
    environment:
      - FLASK_ENV=production
      - DATABASE_URL=postgresql://cloudcare_user:${POSTGRES_PASSWORD}@postgres:5432/cloudcare
      - MONGODB_URI=mongodb://mongodb:27017/cloudcare
      - REDIS_URL=redis://redis:6379/0
    volumes:
      - ./app:/app
    ports:
      - "5000:5000"
    depends_on:
      - postgres
      - mongodb
      - redis
    restart: unless-stopped

  celery_worker:
    build: .
    command: celery -A app.celery worker --loglevel=info
    environment:
      - DATABASE_URL=postgresql://cloudcare_user:${POSTGRES_PASSWORD}@postgres:5432/cloudcare
      - MONGODB_URI=mongodb://mongodb:27017/cloudcare
      - REDIS_URL=redis://redis:6379/0
    depends_on:
      - postgres
      - mongodb
      - redis
    restart: unless-stopped

  celery_beat:
    build: .
    command: celery -A app.celery beat --loglevel=info
    environment:
      - DATABASE_URL=postgresql://cloudcare_user:${POSTGRES_PASSWORD}@postgres:5432/cloudcare
      - REDIS_URL=redis://redis:6379/0
    depends_on:
      - redis
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - app
    restart: unless-stopped

volumes:
  postgres_data:
  mongo_data:
  redis_data:
```

### Kubernetes Deployment

**deployment.yaml:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cloudcare-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: cloudcare-api
  template:
    metadata:
      labels:
        app: cloudcare-api
    spec:
      containers:
      - name: cloudcare-api
        image: cloudcare/api:latest
        ports:
        - containerPort: 5000
        env:
        - name: FLASK_ENV
          value: "production"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: cloudcare-secrets
              key: database-url
        - name: MONGODB_URI
          valueFrom:
            secretKeyRef:
              name: cloudcare-secrets
              key: mongodb-uri
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /health
            port: 5000
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /ready
            port: 5000
          initialDelaySeconds: 5
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: cloudcare-api-service
spec:
  selector:
    app: cloudcare-api
  ports:
    - protocol: TCP
      port: 80
      targetPort: 5000
  type: LoadBalancer
```

---

## Monitoring & Maintenance

### Health Check Endpoint

```python
@app.route('/health', methods=['GET'])
def health_check():
    """System health check"""
    health_status = {
        "status": "healthy",
        "timestamp": datetime.utcnow().isoformat(),
        "services": {}
    }
    
    # Check PostgreSQL
    try:
        db.session.execute('SELECT 1')
        health_status["services"]["postgres"] = "healthy"
    except:
        health_status["services"]["postgres"] = "unhealthy"
        health_status["status"] = "degraded"
    
    # Check MongoDB
    try:
        mongo_db.command('ping')
        health_status["services"]["mongodb"] = "healthy"
    except:
        health_status["services"]["mongodb"] = "unhealthy"
        health_status["status"] = "degraded"
    
    # Check Redis
    try:
        redis_client.ping()
        health_status["services"]["redis"] = "healthy"
    except:
        health_status["services"]["redis"] = "unhealthy"
        health_status["status"] = "degraded"
    
    status_code = 200 if health_status["status"] == "healthy" else 503
    return jsonify(health_status), status_code
```

### Logging

```python
import logging
from logging.handlers import RotatingFileHandler

# Configure logging
handler = RotatingFileHandler(
    'logs/cloudcare.log',
    maxBytes=10485760,  # 10MB
    backupCount=10
)

formatter = logging.Formatter(
    '%(asctime)s %(levelname)s: %(message)s [in %(pathname)s:%(lineno)d]'
)

handler.setFormatter(formatter)
app.logger.addHandler(handler)
app.logger.setLevel(logging.INFO)
```

---

## Conclusion

This backend setup provides:

✅ **Aadhar-based UID system** for universal patient identification  
✅ **Dual database architecture** (PostgreSQL + MongoDB) for optimal data handling  
✅ **Document request system** for cross-hospital record retrieval  
✅ **Real-time health monitoring** via WebSocket  
✅ **Comprehensive security** with encryption, JWT, RBAC  
✅ **Scalable microservices** architecture  
✅ **Production-ready** deployment configurations  

**Next Steps:**
1. Set up development environment
2. Test Aadhar UID generation
3. Integrate with first wearable device
4. Deploy to staging environment
5. Complete security audit
6. Launch beta program

---

**End of Backend Setup Guide**
