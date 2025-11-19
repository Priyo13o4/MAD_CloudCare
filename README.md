````markdown
# CloudCare - Healthcare Management Platform

> A comprehensive healthcare platform connecting patients, doctors, and hospitals through unified digital ecosystem with wearable device integration and Aadhar-based universal patient identification.

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Framework](https://img.shields.io/badge/Framework-Jetpack%20Compose-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Backend](https://img.shields.io/badge/Backend-FastAPI-009688.svg)](https://fastapi.tiangolo.com/)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [Documentation](#documentation)
- [Project Structure](#project-structure)
- [Security & Privacy](#security--privacy)
- [Roadmap](#roadmap)

---

## 🔍 Overview

CloudCare is a modern healthcare management platform connecting patients, doctors, and hospitals through a unified digital ecosystem. Built with **Jetpack Compose** (Android) and **FastAPI** (Backend), the platform features:

- **🔐 Aadhar-Based Universal ID**: Secure patient identification across all healthcare facilities using HMAC-SHA256 UIDs
- **⌚ Wearable Device Integration**: Apple Health/HealthKit support with 30,000+ individual metrics tested
- **📄 Cross-Hospital Data Portability**: Request and consolidate medical records from multiple facilities
- **🎯 Patient-Centric Consent**: Granular, time-limited control over health data access
- **🚨 Real-time Health Monitoring**: Emergency alerts and critical patient tracking for doctors

### Core Innovation

CloudCare solves India's healthcare record fragmentation problem through **Aadhar-based universal patient identification**. Every patient gets a privacy-preserving UID that enables seamless data portability between facilities without exposing their Aadhar number.

---

## ✨ Key Features

### For Patients
- 📱 **Personalized Dashboard** - Health overview with quick stats and actions
- ⌚ **Wearable Integration** - Apple Health/HealthKit with 30K+ metrics tested
- 📄 **Medical Records** - Upload, manage, and share documents securely
- 🔐 **Consent Management** - Granular, time-limited data access control
- 🏥 **Cross-Facility Portability** - Request records from any hospital via Aadhar UID
- 🚨 **Health Alerts** - Real-time notifications for abnormal vitals

### For Doctors
- 📊 **Patient Dashboard** - Overview of assigned patients with status indicators
- 🚨 **Emergency Monitoring** - Real-time alerts with severity tracking (Critical/High/Medium/Low)
- 👥 **Patient Management** - Access full health history with valid consent
- 📅 **Appointment Schedule** - Daily consultations and notes management
- 📝 **Medical Records** - View and update patient records

### For Hospitals
- 🏢 **Facility Dashboard** - Key metrics, bed availability, emergency tracking
- 👨‍⚕️ **Staff Management** - Doctor assignments and specializations
- 🛏️ **Resource Management** - Bed/equipment tracking with low-stock alerts
- 🚑 **Admissions** - Patient admission and department assignment

---

## 🏗️ Architecture

### Application Architecture
```
MVVM (Model-View-ViewModel) Pattern
├── UI Layer (Jetpack Compose)
├── ViewModel Layer (State Management)
├── Repository Layer (Data Abstraction)
└── Data Layer (API, Cache, Local)
```

### Database Architecture
```
Dual Database System
├── PostgreSQL (Relational)
│   ├── Users & Authentication
│   ├── Patient Metadata
│   ├── Consents & Audit Logs
│   └── Device Pairings
└── MongoDB (Document Store)
    ├── Individual Health Metrics
    ├── Wearable Device Data
    └── Medical Documents (GridFS)
```

### Backend
- **Framework**: FastAPI (Python 3.11+)
- **API**: RESTful endpoints with JWT authentication
- **ORM**: Prisma for PostgreSQL
- **Deployment**: Docker + Cloudflare Tunnel for stable URL

---

## 📚 Documentation

### Quick Start Guides

| Document | Purpose |
|----------|----------|
| **[backend/README.md](backend/README.md)** | Backend quick start - Docker setup & environment config |
| **[docs/BACKEND_SETUP_GUIDE.md](docs/BACKEND_SETUP_GUIDE.md)** | Complete backend setup - API endpoints, databases, deployment |
| **[docs/BACKEND_DATA_MODEL.md](docs/BACKEND_DATA_MODEL.md)** | Database schemas for Patient & Doctor entities |

### Feature Documentation

| Document | Purpose |
|----------|----------|
| **[docs/APPLE_HEALTH_INTEGRATION.md](docs/APPLE_HEALTH_INTEGRATION.md)** | Apple Health/HealthKit integration guide |
| **[docs/IOS_QR_PAIRING_PROMPT.md](docs/IOS_QR_PAIRING_PROMPT.md)** | QR code device pairing (iOS ↔ Android) |
| **[docs/DEVICE_UNPAIR_IMPLEMENTATION.md](docs/DEVICE_UNPAIR_IMPLEMENTATION.md)** | Device unpairing and data cleanup |

### How to Navigate
1. **First Time Setup**: [backend/README.md](backend/README.md) → [docs/BACKEND_SETUP_GUIDE.md](docs/BACKEND_SETUP_GUIDE.md)
2. **Database Schema**: [docs/BACKEND_DATA_MODEL.md](docs/BACKEND_DATA_MODEL.md)
3. **Wearable Integration**: [docs/APPLE_HEALTH_INTEGRATION.md](docs/APPLE_HEALTH_INTEGRATION.md)

---

## 🛠️ Technology Stack

### Android App
| Component | Technology |
|-----------|-----------|
| Language | Kotlin 2.0.21 |
| UI Framework | Jetpack Compose |
| Architecture | MVVM |
| Networking | Retrofit 2.9.0 |
| Async | Coroutines 1.7.3 |
| Material Design | Material 3 |

### Backend
| Component | Technology |
|-----------|-----------|
| Framework | FastAPI 3.0.0 |
| Language | Python 3.11+ |
| ORM | Prisma |
| Databases | PostgreSQL 15+, MongoDB 6.0+ |
| Cache | Redis 7+ |
| Deployment | Docker |

---

## 🚀 Getting Started

### Prerequisites

#### Android Development
- Android Studio Ladybug+
- JDK 11+
- Android SDK API 26+
- Gradle 8.13.0+

#### Backend Development
- Python 3.11+
- Docker & Docker Compose
- PostgreSQL 15+
- MongoDB 6.0+

### Quick Start - Android App

```bash
# 1. Clone and navigate
git clone https://github.com/Priyo13o4/MAD_CloudCare.git
cd MAD_CloudCare/CloudCare\ Android\ App

# 2. Open in Android Studio
# File → Open → Select CloudCare Android App folder

# 3. Build and run
./gradlew assembleDebug
./gradlew installDebug

# 4. Connect to backend
# Update BASE_URL in RetrofitClient.kt to:
# https://cloudcare.pipfactor.com/api/v1/
```

### Quick Start - Backend

```bash
# 1. Navigate to backend
cd MAD_CloudCare/backend

# 2. Copy environment
cp .env.example .env

# 3. Start services
docker-compose up -d

# 4. Run migrations
docker-compose exec api prisma migrate dev

# 5. Access at https://cloudcare.pipfactor.com
```

**Full setup instructions**: See [docs/BACKEND_SETUP_GUIDE.md](docs/BACKEND_SETUP_GUIDE.md)

---

## 📁 Project Structure

```
MAD_CloudCare/
├── CloudCare Android App/          # Android app (Jetpack Compose)
│   ├── app/
│   │   ├── src/main/java/          # Kotlin source
│   │   └── src/main/res/           # Resources
│   └── build.gradle.kts
├── CloudSync/                       # iOS companion app (SwiftUI)
├── backend/                         # FastAPI backend
│   ├── app/
│   │   ├── api/                    # API routes
│   │   ├── services/               # Business logic
│   │   ├── models/                 # Data models
│   │   └── core/                   # Configuration
│   ├── prisma/                     # Database schemas
│   ├── docker-compose.yml          # Services setup
│   └── requirements.txt            # Python dependencies
├── docs/                           # Documentation
│   ├── CLOUDCARE_DOCUMENTATION.md
│   ├── BACKEND_SETUP_GUIDE.md
│   ├── APPLE_HEALTH_INTEGRATION.md
│   └── IOS_QR_PAIRING_PROMPT.md
└── README.md                       # This file
```

---

## 🔑 Key Implementation Details

### Aadhar-Based Patient Identification

CloudCare uses India's Aadhar as the foundation for patient identification:

```
Patient Registration
    ↓
Aadhar Number Input
    ↓
Generate HMAC-SHA256 UID (irreversible)
    ↓
Encrypt Original Aadhar (separate storage)
    ↓
Use UID for all data linking
```

**Benefits:**
- ✅ Universal identification across hospitals
- ✅ Prevents record duplication
- ✅ Enables document portability
- ✅ Future-ready for ABDM integration
- ✅ Privacy-preserving (raw Aadhar not exposed)

### Wearable Device Integration

**Individual Metrics Storage** (not aggregated):
- Each health reading stored separately
- 27,185+ metrics successfully tested
- Multi-level deduplication:
  - iOS app-level detection
  - Backend database-level validation
  - Unique compound indexes: `(patient_id, device_id, metric_type, timestamp)`

**Supported Integration Methods:**
- Apple Health (JSON export from iOS)
- QR Code device pairing (iOS ↔ Android linking)
- Direct API sync for health data

### Database Architecture Rationale

**PostgreSQL for Structured Data:**
- User authentication (ACID transactions)
- Patient metadata (relational integrity)
- Consent management (audit trails)
- Device pairings (referential integrity)

**MongoDB for Health Data:**
- Individual health metrics (high write throughput)
- Time-series optimization (streaming data)
- Flexible schema (device type variations)
- GridFS for medical documents

---

## 🔒 Security & Privacy

### Data Protection
- ✅ Encryption at rest (database level)
- ✅ Encryption in transit (HTTPS/TLS 1.2+)
- ✅ JWT authentication (secure tokens)
- ✅ RBAC (role-based access control)

### Privacy Features
- ✅ Patient-controlled consent system
- ✅ Audit logs (all access tracked)
- ✅ Data minimization (only necessary info)
- ✅ Right to delete (GDPR ready)

### Compliance
- HIPAA-compliant architecture
- GDPR-ready data handling
- India's DPDPA (Digital Personal Data Protection Act) alignment

---

## 📊 Project Status

**Status**: 🟢 **Active Development** | **Last Updated**: December 2024 | **Version**: 1.0.0-beta

### ✅ Completed Features
- **Architecture**: Complete MVVM with Jetpack Compose UI
- **Backend**: FastAPI + PostgreSQL + MongoDB + Redis
- **Wearables**: Apple Health/HealthKit (30K+ metrics tested)
- **Data Flow**: Comprehensive single-endpoint architecture (73% faster)
- **Timezone**: IST (UTC+5:30) conversion across all timestamps
- **Pairing**: QR code device linking (iOS ↔ Android)
- **Security**: JWT authentication + Aadhar UID encryption
- **Deployment**: Docker + Cloudflare Tunnel (stable URL)
- **Caching**: Multi-layer cache with hourly/daily/monthly aggregation

### 🚧 In Progress
- Real-time Apple Watch sync (background uploads)
- AI-powered health insights (TimeGPT integration)
- Doctor telemedicine features

### ⏳ Planned
- Google Fit / Fitbit / Xiaomi Mi Band integration
- ABDM (Ayushman Bharat) integration
- E-pharmacy linking
- Multi-language support (Hindi, regional languages)

---

## 🤝 Contributing

Contributions welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

**Guidelines:**
- Follow Kotlin conventions for Android
- Write clean, documented code
- Add tests for new features
- Update documentation

---

## 📄 License

This project is licensed under the MIT License - see LICENSE file for details.

---

## 📞 Support & Contact

- **Documentation**: See [docs/](docs/) folder
- **Issues**: Create an issue in the repository
- **Questions**: Check existing issues first

---

## 👥 Team

Developed by students of MAD (Mobile Application Development) course, Semester 5.

---

**Made with ❤️ for better healthcare in India**

````

## ✨ Features

### For Patients

#### 📱 Dashboard
- Personalized health overview
- Quick stats (linked facilities, records, consents, devices)
- Recent activity feed
- Quick action shortcuts

#### ⌚ Wearables & Health Tracking
- Connect fitness trackers and smartwatches (Xiaomi Mi Band, Fitbit, Apple Watch, etc.)
- **Apple Health Integration**: Import health data directly from iPhone/Apple Watch (JSON exports)
- **Individual Metrics Storage**: Each health reading stored separately (27,185+ metrics tested)
- **Multi-Level Deduplication**: iOS app-level + backend database-level duplicate prevention
- **QR Code Pairing**: Securely link Apple Watch to Android account via QR code
- Real-time health metrics (steps, heart rate, sleep, calories, oxygen saturation, blood pressure)
- Historical data visualization with batch import support
- Health insights and trends
- Goal tracking with progress indicators
- Automatic health alerts for abnormal vitals
- **Time-Series Ready**: Optimized for AI/ML analysis (TimeGPT, Gemini Flash)

#### 📄 Medical Records
- View and manage all medical records
- Upload documents (PDFs, images)
- Filter by type (Lab Reports, Prescriptions, Consultations, Imaging)
- Secure cloud storage
- Share records with healthcare providers

#### 🔐 Consent Management
- Review consent requests from doctors/hospitals
- Approve/deny data access
- Granular control over data categories
- Revoke access anytime
- Consent history and audit logs

#### 🏥 Linked Facilities
- View all connected healthcare facilities
- Multiple facility types (Hospitals, Clinics, Labs, Pharmacies)
- Track patient IDs across facilities
- Request records from facilities

#### 📋 Document Request System (NEW)
- Request medical records from hospitals not yet on CloudCare
- Track request status
- Receive notifications when documents are uploaded

#### 👤 Profile Management
- Personal information
- Emergency contacts
- Insurance details
- Privacy settings

### For Doctors

#### 📊 Doctor Dashboard
- Patient management overview
- Today's appointment schedule
- Emergency alerts feed
- Quick stats

#### 👥 Patient Management
- View assigned patients
- Patient status indicators (Stable, Monitoring, Critical)
- Emergency flags
- Access patient history (with consent)

#### 🚨 Emergency Monitoring
- Real-time health alerts
- Severity levels (Critical, High, Medium, Low)
- Alert types (Heart Rate, Oxygen Level, Blood Pressure, Temperature)
- Quick response actions

#### 📅 Appointment Schedule
- Daily schedule view
- Patient information preview
- Appointment status tracking
- Add consultation notes

#### 📝 Medical Records
- View patient medical history (with consent)
- Add consultation notes
- Prescribe medications
- Order tests

### For Hospitals

#### 🏢 Hospital Dashboard
- Key metrics overview
- Bed availability tracking
- Emergency cases monitoring
- Response time analytics

#### 👨‍⚕️ Staff Management
- View all hospital staff
- Specializations and departments
- Patient assignments
- Availability status

#### 🛏️ Resource Management
- Bed management (total vs. available)
- Equipment tracking
- Supply inventory
- Low-stock alerts

#### 🚑 Admissions
- View admitted patients
- Emergency admissions
- Department assignments
- Patient monitoring

---

## 🏗️ Architecture

CloudCare follows a modern, scalable architecture:

### Android App Architecture
```
MVVM (Model-View-ViewModel)
├── UI Layer (Jetpack Compose)
│   ├── Screens
│   ├── Components
│   └── Navigation
├── ViewModel Layer
│   ├── State Management
│   ├── Business Logic
│   └── Data Transformation
├── Repository Layer
│   └── Data Source Abstraction
└── Data Layer
    ├── Remote (API)
    ├── Local (Cache)
    └── Models
```

### Backend Architecture
```
Monolithic FastAPI Application
├── API Gateway (NGINX)
├── Auth Module (FastAPI)
├── Wearables Module (FastAPI)
├── Medical Records Module (FastAPI)
├── Consent Module (FastAPI)
├── Notification Module (FastAPI)
└── Document Request Module (FastAPI)
```

### Database Architecture
```
Dual Database System
├── PostgreSQL (Relational Data)
│   ├── Users & Authentication (JWT)
│   ├── Patient Metadata (Aadhar-based UIDs)
│   ├── Consents (Time-limited, revocable)
│   ├── Facilities (Hospitals, clinics, labs)
│   ├── Device Pairings (iOS ↔ Android linking)
│   └── Audit Logs
└── MongoDB (Document Store)
    ├── Health Metrics (Individual readings, not aggregated)
    │   ├── 27,185+ metrics tested
    │   ├── Indexed: (patient_id, device_id, metric_type, timestamp)
    │   ├── Types: heart_rate, steps, calories, distance, flights_climbed, resting_heart_rate, vo2_max
    │   └── Deduplication via unique compound index
    ├── Wearable Data (Device sync status)
    ├── Medical Documents (GridFS)
    └── Real-time Alerts
```

---

## 📚 Documentation

Comprehensive documentation is available in the following files:

1. **[docs/CLOUDCARE_DOCUMENTATION.md](docs/CLOUDCARE_DOCUMENTATION.md)**
   - Complete app documentation
   - Features overview
   - Architecture details
   - Data models
   - UI components
   - User flows

2. **[docs/BACKEND_SETUP_GUIDE.md](docs/BACKEND_SETUP_GUIDE.md)**
   - Complete backend setup instructions
   - Aadhar-based UID system design
   - Database schemas (PostgreSQL + MongoDB)
   - API endpoint documentation
   - Document request system
   - Security implementation
   - Deployment guide

3. **[docs/COMPREHENSIVE_ENDPOINT_IMPLEMENTATION.md](docs/COMPREHENSIVE_ENDPOINT_IMPLEMENTATION.md)** ⭐ NEW
   - Single API endpoint for all health metrics
   - Replaces 5+ individual endpoints
   - Fixes card synchronization bugs
   - Performance improvements
   - Complete implementation guide

4. **[docs/IST_TIMEZONE_IMPLEMENTATION.md](docs/IST_TIMEZONE_IMPLEMENTATION.md)** ⭐
   - IST (Asia/Kolkata, UTC+5:30) timezone conversion
   - TimeFormatter utility class
   - Extension functions for all data models
   - Updated UI screens with IST timestamps
   - Database verification (Apple Watch device)

5. **[docs/TESTING_COMPREHENSIVE_ENDPOINT.md](docs/TESTING_COMPREHENSIVE_ENDPOINT.md)** ⭐ NEW
   - Complete testing guide for comprehensive metrics
   - Backend endpoint verification
   - Android integration testing
   - Test scenarios and debugging

6. **[docs/APPLE_HEALTH_INTEGRATION.md](docs/APPLE_HEALTH_INTEGRATION.md)**
   - Apple Health/HealthKit integration guide
   - Supported metrics (7+ types)
   - API endpoints for import (single & batch)
   - Swift/Kotlin code examples
   - Testing with 21 sample files
   - **Individual metrics storage** (not aggregated)
   - Deduplication strategy
   - Troubleshooting guide

7. **[docs/IOS_QR_PAIRING_PROMPT.md](docs/IOS_QR_PAIRING_PROMPT.md)**
   - QR code pairing feature implementation guide
   - iOS CloudSync app pairing UI
   - Pairing data structure and security
   - SwiftUI code examples
   - Android integration instructions

8. **[backend/README.md](backend/README.md)**
   - Backend API documentation
   - FastAPI setup and configuration
   - Database connections (PostgreSQL, MongoDB, Redis)
   - Prisma ORM usage
   - Docker deployment
   - Environment variables

---

## 🛠️ Technology Stack

### Android App

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Kotlin | 2.0.21 |
| UI Framework | Jetpack Compose | BOM 2024.09.00 |
| Architecture | MVVM | - |
| Navigation | Navigation Compose | 2.8.2 |
| Material Design | Material 3 | 1.3.0 |
| Networking | Retrofit | 2.9.0 |
| Image Loading | Coil | 2.7.0 |
| Async | Coroutines | 1.7.3 |
| DI | Hilt (optional) | 2.51.1 |

### Backend

| Framework | Technology | Version |
|-----------|-----------|---------|
| Framework | FastAPI | 3.0.0 |
| Language | Python | 3.11+ |
| ORM | Prisma | Latest |
| API | RESTful | - |
| Authentication | JWT | - |
| Logging | structlog | Latest |
| Cache | Redis | 7+ |
| Tunnel | Cloudflare | - |

### Databases

| Database | Use Case | Version |
|----------|----------|---------|
| PostgreSQL | User auth, metadata, consents, device pairings | 15+ |
| MongoDB | Individual health metrics (27K+ tested), documents | 6.0+ |
| Redis | Cache, sessions, real-time data | 7+ |

### Infrastructure

- **API Gateway**: NGINX
- **File Storage**: AWS S3 / MinIO
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack
- **Container**: Docker
- **Orchestration**: Kubernetes (production)

---

## 🚀 Getting Started

### Prerequisites

#### For Android Development
- Android Studio Ladybug or later
- JDK 11 or later
- Android SDK with API 26+
- Gradle 8.13.0+

#### For Backend Development
- Python 3.11+
- PostgreSQL 15+
- MongoDB 6.0+
- Redis 7+

### Android App Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourorg/cloudcare.git
   cd cloudcare/CloudCare\ Android\ App
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - File → Open → Select `CloudCare Android App` folder
   - Wait for Gradle sync

3. **Build and Run**
   ```bash
   # Build debug APK
   ./gradlew assembleDebug
   
   # Install on connected device
   ./gradlew installDebug
   ```

4. **Run the app**
   - Connect Android device or start emulator
   - Click Run button in Android Studio
   - Or use: `./gradlew installDebug`

### Backend Setup

See **[BACKEND_SETUP_GUIDE.md](BACKEND_SETUP_GUIDE.md)** for complete instructions.

**Quick Start:**

1. **Clone backend code**
   ```bash
   git clone https://github.com/yourorg/cloudcare-backend.git
   cd cloudcare-backend
   ```

2. **Create virtual environment**
   ```bash
   python3 -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

3. **Install dependencies**
   ```bash
   pip install -r requirements.txt
   ```

4. **Setup databases**
   ```bash
   # PostgreSQL
   createdb cloudcare
   python manage.py db upgrade
   
   # MongoDB (auto-creates)
   python scripts/create_mongo_indexes.py
   ```

5. **Configure environment**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

6. **Run development server**
   ```bash
   python app.py
   ```

### Connect App to Backend

See **[ANDROID_BACKEND_INTEGRATION.md](ANDROID_BACKEND_INTEGRATION.md)** for complete integration steps.

**Quick Integration:**

1. Use the Cloudflare Tunnel URL in your app:
   ```kotlin
   private const val BASE_URL = "https://cloudcare.pipfactor.com/api/v1/"
   ```

2. Add dependencies to `app/build.gradle.kts`:
   ```kotlin
   implementation("com.squareup.retrofit2:retrofit:2.9.0")
   implementation("com.squareup.retrofit2:converter-gson:2.9.0")
   ```

3. Test connection by running the app with backend running

---

## 🔧 Backend Setup

### The Aadhar-Based UID System

CloudCare uses India's Aadhar number as the foundation for patient identification:

**Why Aadhar?**
- ✅ Universal identification across all hospitals in India
- ✅ Prevents patient record duplication
- ✅ Enables document portability between facilities
- ✅ Future-ready for ABDM integration
- ✅ Privacy-preserving (we don't store raw Aadhar)

**How it works:**
1. Patient registers with Aadhar number
2. System generates irreversible UID using HMAC-SHA256
3. Original Aadhar is encrypted and stored separately
4. UID is used for all data linking across facilities

**Example:**
```python
aadhar_number = "1234 5678 9012"
generated_uid = "a3f5e8d9c2b1a0f4e3d2c1b0a9f8e7d6..."  # 64 chars
```

This UID is consistent across all facilities and enables:
- Requesting records from any hospital using the UID
- Consolidating health data from multiple sources
- Patient identification without exposing Aadhar

### Database Choice Rationale

#### PostgreSQL for Structured Data
- **User authentication**: ACID compliance for critical auth data
- **Patient metadata**: Relational integrity between patients and facilities
- **Consents**: Audit trails and expiration tracking
- **Facilities**: Structured facility information

#### MongoDB for Health Data
- **Wearable metrics**: High write throughput for streaming data
- **Time-series data**: Efficient storage and querying
- **Medical documents**: GridFS for large files
- **Flexible schema**: Health metrics vary by device type

**This dual-database approach provides:**
- Best-of-both-worlds data storage
- Optimal performance for each data type
- Scalability for high-frequency wearable data
- Data integrity for critical information

---

## 📱 App Screenshots

### Patient App
- **Splash & Login**: Beautiful onboarding experience
- **Dashboard**: Health overview with quick actions
- **Wearables**: Device management and health tracking
- **Records**: Medical record management
- **Consents**: Privacy-first consent system

### Doctor App
- **Dashboard**: Patient overview and emergency alerts
- **Patients**: Assigned patient list with status
- **Emergency**: Critical patient monitoring
- **Schedule**: Appointment management

### Hospital App
- **Dashboard**: Facility metrics and bed availability
- **Staff**: Staff management and assignments
- **Resources**: Inventory and equipment tracking
- **Admissions**: Patient admission management

---

## 🔒 Security & Privacy

CloudCare implements multiple layers of security:

### Data Protection
- ✅ **Encryption at Rest**: All sensitive data encrypted in database
- ✅ **Encryption in Transit**: HTTPS/TLS 1.2+ for all communications
- ✅ **Aadhar Protection**: Original Aadhar numbers encrypted, never exposed
- ✅ **JWT Authentication**: Secure token-based authentication
- ✅ **RBAC**: Role-based access control

### Privacy Features
- ✅ **Consent System**: Patient controls all data access
- ✅ **Audit Logs**: All data access logged and trackable
- ✅ **Data Minimization**: Only collect necessary information
- ✅ **Anonymization**: Patient UIDs don't reveal identity
- ✅ **Right to Delete**: Patients can delete their data

### Compliance Ready
- HIPAA-compliant architecture
- GDPR-ready data handling
- India's Digital Personal Data Protection Act (DPDPA) alignment

---

## 🌟 Key Innovations

### 1. Aadhar-Based Universal Patient ID
First-of-its-kind implementation using Aadhar for healthcare record portability across India.

### 2. Cross-Hospital Document Request
Patients can request medical records from any hospital, even if not on CloudCare yet. Hospitals are notified and can upload records.

### 3. Real-time Wearable Integration
Continuous health monitoring with AI-powered insights and emergency alerts.

### 4. Patient-Controlled Consent
Granular, time-limited consent system giving patients complete control over their health data.

### 5. Dual Database Architecture
Optimized data storage using PostgreSQL for structured data and MongoDB for health metrics.

---

## 🔮 Roadmap

### Phase 1: MVP (✅ Complete)
- ✅ Patient, Doctor, Hospital apps
- ✅ Mock data implementation
- ✅ UI/UX complete
- ✅ Navigation flows

### Phase 2: Backend Integration (✅ Complete)
- ✅ FastAPI backend API
- ✅ PostgreSQL + MongoDB setup
- ✅ Aadhar-based UID system
- ✅ JWT authentication
- ✅ Cloudflare Tunnel (stable URL)
- ✅ Docker containerization

### Phase 3: Wearable Integration (✅ Complete)
- ✅ Apple Health/HealthKit integration (JSON import)
- ✅ Individual metrics storage (27,185+ metrics tested)
- ✅ Multi-level deduplication (iOS + Backend)
- ✅ QR code device pairing (iOS ↔ Android)
- ✅ Backend API endpoints (7+ metric types)
- 🚧 Real-time Apple Watch sync

### Phase 4: API Architecture Optimization (✅ Complete)
- ✅ **Comprehensive single-endpoint** implementation
- ✅ Replaces 5+ individual API calls
- ✅ Fixes card synchronization bugs
- ✅ Improves performance (73% faster)
- ✅ IST timezone parsing for all timestamps
- ✅ Better caching strategy

### Phase 5: Advanced Features
- ⏳ Real-time health insights
- ⏳ AI-powered health monitoring
- ⏳ Telemedicine integration

### Phase 6: Integration & Expansion
- ⏳ Google Fit integration
- ⏳ Xiaomi Mi Band SDK
- ⏳ Fitbit API
- ⏳ ABDM integration (Ayushman Bharat)

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Write clean, documented code
- Add unit tests for new features
- Update documentation

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 👥 Team

Developed by students of MAD (Mobile Application Development) course, Semester 5.

---

## 📞 Support

For support, email support@cloudcare.com or open an issue in the repository.

---

## 🙏 Acknowledgments

- Material Design 3 by Google
- Jetpack Compose team
- Flask community
- Open source contributors

---

## 📊 Project Status

**Status**: 🟢 Active Development

**Last Updated**: November 14, 2025

**Version**: 1.0.0-beta

---

**Made with ❤️ for better healthcare in India**
