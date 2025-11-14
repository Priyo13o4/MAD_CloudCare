# CloudCare - Healthcare Management Platform

> A comprehensive Android application for patient health management, doctor-patient coordination, and hospital administration with wearable device integration and Aadhar-based patient identification.

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Framework](https://img.shields.io/badge/Framework-Jetpack%20Compose-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Backend](https://img.shields.io/badge/Backend-Flask-lightgrey.svg)](https://flask.palletsprojects.com/)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Documentation](#documentation)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [Backend Setup](#backend-setup)
- [App Screenshots](#app-screenshots)
- [Contributing](#contributing)
- [License](#license)

---

## 🔍 Overview

CloudCare is a modern healthcare management platform that connects patients, doctors, and hospitals through a unified digital ecosystem. The application features:

- **Patient-Centric Data Control**: Patients have complete control over their health data with granular consent management
- **Real-time Health Monitoring**: Integration with wearable devices for continuous health tracking
- **Aadhar-Based Universal ID**: Using India's Aadhar system as the foundation for patient identification across healthcare facilities
- **Cross-Hospital Data Portability**: Patients can request and consolidate medical records from multiple hospitals
- **Emergency Response System**: Real-time alerts and critical patient monitoring for doctors

### 🎯 Key Objectives

1. **Unified Health Records**: Consolidate health data from multiple sources into one secure platform
2. **Privacy-First Design**: Patient-controlled consent system for all data sharing
3. **Seamless Healthcare Coordination**: Enable efficient communication between patients, doctors, and hospitals
4. **Preventive Healthcare**: Continuous monitoring and AI-powered health insights
5. **Universal Accessibility**: Works across all healthcare facilities using Aadhar-based identification

---

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
- Real-time health metrics (steps, heart rate, sleep, calories, oxygen saturation, blood pressure)
- Historical data visualization with batch import support
- Health insights and trends
- Goal tracking with progress indicators
- Automatic health alerts for abnormal vitals

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
Microservices Architecture
├── API Gateway (NGINX)
├── Auth Service (Flask)
├── Wearables Service (Flask)
├── Medical Records Service (Flask)
├── Consent Service (Flask)
├── Notification Service (Flask)
└── Document Request Service (Flask)
```

### Database Architecture
```
Dual Database System
├── PostgreSQL (Relational Data)
│   ├── Users & Authentication
│   ├── Patient Metadata
│   ├── Consents
│   ├── Facilities
│   └── Audit Logs
└── MongoDB (Document Store)
    ├── Health Metrics (Time-Series)
    ├── Wearable Data
    ├── Medical Documents
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

3. **[docs/ANDROID_BACKEND_INTEGRATION.md](docs/ANDROID_BACKEND_INTEGRATION.md)**
   - Step-by-step integration guide
   - Retrofit setup
   - API service interfaces
   - Token management
   - Background sync
   - Testing instructions

4. **[APPLE_HEALTH_INTEGRATION.md](APPLE_HEALTH_INTEGRATION.md)**
   - Apple Health/HealthKit integration guide
   - Supported metrics (8+ types)
   - API endpoints for import (single & batch)
   - Swift/Kotlin code examples
   - Testing with 21 sample files
   - Data parsing and aggregation
   - Troubleshooting guide

5. **[backend/README.md](backend/README.md)**
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

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Flask | 3.0.0 |
| Language | Python | 3.11+ |
| API | RESTful | - |
| Authentication | JWT | - |
| Real-time | Socket.IO | 5.10.0 |
| Task Queue | Celery | 5.3.4 |
| Cache | Redis | 7+ |

### Databases

| Database | Use Case | Version |
|----------|----------|---------|
| PostgreSQL | User auth, metadata, consents | 15+ |
| MongoDB | Health data, documents | 6.0+ |
| Redis | Cache, sessions | 7+ |

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

1. Update `BASE_URL` in `RetrofitClient.kt`:
   ```kotlin
   private const val BASE_URL = "http://YOUR_IP:5000/"
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

### Phase 1: MVP (Current)
- ✅ Patient, Doctor, Hospital apps
- ✅ Mock data implementation
- ✅ UI/UX complete
- ✅ Navigation flows

### Phase 2: Backend Integration (In Progress)
- 🚧 Flask backend API
- 🚧 PostgreSQL + MongoDB setup
- 🚧 Aadhar-based UID system
- 🚧 JWT authentication

### Phase 3: Wearable Integration
- ✅ Apple Health/HealthKit integration (JSON import)
- 🚧 Real-time Apple Watch sync
- ⏳ Google Fit integration
- ⏳ Xiaomi Mi Band SDK
- ⏳ Fitbit API

### Phase 4: Advanced Features
- ⏳ AI-powered health insights
- ⏳ Telemedicine integration
- ⏳ Prescription e-pharmacy link
- ⏳ Lab report parsing with OCR

### Phase 5: ABDM Integration
- ⏳ Ayushman Bharat Digital Mission integration
- ⏳ ABHA (Ayushman Bharat Health Account) linking
- ⏳ Health Information Exchange (HIE)

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
