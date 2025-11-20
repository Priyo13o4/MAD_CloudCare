# CloudCare - Healthcare Management Platform

> Unified healthcare ecosystem connecting patients, doctors, and hospitals with wearable device integration and Aadhar-based patient identification.

[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-blue)]()
[![Backend](https://img.shields.io/badge/Backend-FastAPI-009688)]()
[![License](https://img.shields.io/badge/License-MIT-green)]()

---

## Overview

CloudCare is a comprehensive healthcare management platform enabling:
- **Patients**: Health tracking, medical records, consent management
- **Doctors**: Patient monitoring, emergency alerts, consultation
- **Hospitals**: Dashboard metrics, resource management, admissions

**Live Backend:** `https://cloudcare.pipfactor.com/api/v1`

---

## Key Features

- ⚕️ **Aadhar-Based Universal ID** - Privacy-preserving patient identification across facilities
- ⌚ **Wearable Integration** - Apple Health/HealthKit with 30K+ metrics tested
- 🔐 **Patient Consent System** - Granular, time-limited data access control
- 📄 **Cross-Facility Records** - Document portability between hospitals
- 🚨 **Real-Time Monitoring** - Emergency alerts and critical patient tracking
- 📱 **Multi-Platform** - Android app + iOS companion app

---

## Technology Stack

| Component | Technology |
|-----------|-----------|
| **Android App** | Kotlin, Jetpack Compose, MVVM |
| **iOS App** | Swift, SwiftUI, HealthKit |
| **Backend** | Python, FastAPI 3.0 |
| **Databases** | PostgreSQL 15+, MongoDB 6.0+ |
| **Deployment** | Docker Compose, Cloudflare Tunnel |

---

## Quick Start

### Android App
```bash
cd "CloudCare Android App"
./gradlew assembleDebug
./gradlew installDebug
```

### Backend
```bash
cd backend
docker-compose up -d
```

Backend accessible at: `http://localhost:8000` (or tunnel URL)

### iOS App
```bash
cd CloudSync
open CloudSync.xcodeproj
# Build and run in Xcode
```

---

## Documentation

| Document | Purpose |
|----------|---------|
| **[BACKEND_API.md](docs/BACKEND_API.md)** | Complete API reference, all endpoints, authentication |
| **[ANDROID_APP.md](docs/ANDROID_APP.md)** | Android app architecture, features, setup |
| **[IOS_APP.md](docs/IOS_APP.md)** | iOS app features, HealthKit integration, QR pairing |
| **[DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md)** | PostgreSQL & MongoDB schema, relationships, indexes |

---

## Architecture

```
Android App (Kotlin/Compose)
     ↓ HTTPS/REST
iOS App (Swift/SwiftUI)
     ↓ HTTPS/REST
FastAPI Backend (Python)
     ↓
PostgreSQL (Users, Patients, Doctors, Hospitals, Consents)
     +
MongoDB (Health Metrics: 30K+ documents)
```

**Why Dual Databases?**
- **PostgreSQL**: ACID compliance for auth, structured data, relationships
- **MongoDB**: High throughput for streaming health metrics, time-series data

---

## Setup

### Prerequisites
- **Android**: Android Studio, JDK 11+, SDK 26+
- **iOS**: Xcode 15+, iOS 16+
- **Backend**: Docker, Docker Compose

### Environment Configuration

**Backend** (`.env`):
```bash
DATABASE_URL=postgresql://cloudcare:password@postgres:5432/cloudcare_db
MONGODB_URL=mongodb://mongodb:27017/cloudcare_wearables
JWT_SECRET_KEY=your-secret-key
AADHAR_HMAC_SECRET=your-256-bit-secret
```

**Android** (`RetrofitClient.kt`):
```kotlin
private const val BASE_URL = "https://cloudcare.pipfactor.com/api/v1/"
```

**iOS** (`SettingsView.swift`):
```swift
serverURL: "https://cloudcare.pipfactor.com/api/v1"
```

---

## Database

**PostgreSQL Tables (20):**
- `users` - Authentication (all roles)
- `patients` - Patient profiles (Aadhar UID)
- `doctors` - Doctor credentials
- `hospitals` - Hospital profiles (auto-generated code)
- `consents` - Data access permissions
- `wearable_devices` - Device registrations
- `device_pairings` - iOS-Android linking
- `medical_records` - Document metadata
- `emergency_alerts` - Critical health alerts
- `audit_logs` - Complete audit trail
- ...and 10 more

**MongoDB Collections (1):**
- `health_metrics` - 30,186 individual readings
  - 7+ metric types (heart rate, steps, calories, etc.)
  - Minute-level granularity
  - Compound unique index for deduplication

---

## API Endpoints (Key)

```
POST   /auth/signup/patient      - Patient registration
POST   /auth/signup/doctor       - Doctor registration
POST   /auth/login               - Login (all roles)

GET    /wearables/metrics/comprehensive  - All health metrics (single call)
POST   /wearables/import/apple-health    - Apple Health import
GET    /wearables/summary/today          - Today's summary

GET    /consents/patient/{id}    - Get patient consents
PATCH  /consents/{id}            - Approve/deny/revoke consent

GET    /doctors/{id}/patients    - Assigned patients
GET    /hospitals/{id}/dashboard - Hospital metrics
```

Full API docs: `https://cloudcare.pipfactor.com/docs`

---

## Security

- ✅ **Aadhar UID**: HMAC-SHA256 irreversible hash (64 chars)
- ✅ **Original Aadhar**: AES-256-GCM encrypted backup
- ✅ **Passwords**: bcrypt hashing (12 rounds)
- ✅ **JWT Tokens**: 30-min access, 7-day refresh
- ✅ **HTTPS/TLS**: All communications encrypted
- ✅ **RBAC**: Role-based endpoint access
- ✅ **Audit Logs**: All data access tracked

---

## Key Innovations

### 1. Aadhar-Based Universal ID
```python
# Patient registers with Aadhar: "1234 5678 9012"
aadhar_uid = hmac_sha256(aadhar_number)  # → "a3f5e8d9c2b1..."
# Same UID across all hospitals → no duplication
```

### 2. Comprehensive Metrics Endpoint
```http
GET /wearables/metrics/comprehensive?patient_id=uuid&days=30
# Returns ALL metrics in 1 call (73% faster than 5+ calls)
# Daily aggregated (30 days) + hourly (24 hours)
```

### 3. Patient Consent System
```
Doctor scans QR → Consent PENDING
Patient approves → Consent APPROVED → Doctor gets ACTIVE status
Patient revokes → Consent REVOKED → Doctor status → LOCKED
```

### 4. iOS-Android Device Pairing
```
iOS app generates QR code → Android scans → Paired
Health data from Apple Watch → Syncs to Android account
```

---

## Testing

**Test Data:**
- Patient ID: `3228128A-7110-4D47-8EDB-3A9160E3808A`
- Health Metrics: 30,186 documents (34 days of Apple Watch data)
- Device: Apple Watch Series 9, watchOS 11.1
- Last Sync: 2025-11-19 09:00:13 UTC (14:30 IST)

**Test Scenarios:**
1. Login as patient → Dashboard loads with stats
2. Navigate to Wearables → Health metrics display (steps, heart rate, etc.)
3. Tap Daily (D) → Hourly data shows
4. Navigate to Consents → Approve/deny pending requests
5. Login as doctor → My Patients loads
6. Tap patient (ACTIVE) → View Details opens with full data
7. Login as hospital → Dashboard shows bed availability, metrics

---

## Performance

- **API Response**: Sub-second for comprehensive endpoint
- **MongoDB Queries**: Sub-second on 30K+ documents
- **Comprehensive Endpoint**: 73% faster (1 call vs 5+)
- **Caching**: Disk cache for offline support
- **Indexing**: Compound indexes for fast queries

---

## Compliance

- ✅ HIPAA-compliant architecture
- ✅ GDPR-ready data handling
- ✅ India DPDPA alignment
- ✅ Audit logs for all access
- ✅ Patient right to delete

---

## Project Structure

```
CloudCare/
├── CloudCare Android App/    # Android app (Kotlin/Compose)
│   ├── app/src/main/java/
│   │   ├── data/             # Models, API, repositories
│   │   ├── ui/               # Screens, components
│   │   └── utils/            # Utilities
│   └── build.gradle.kts
│
├── CloudSync/                 # iOS app (Swift/SwiftUI)
│   ├── HealthKitManager.swift
│   ├── NetworkManager.swift
│   ├── PairingQRView.swift
│   └── Models.swift
│
├── backend/                   # FastAPI backend
│   ├── app/
│   │   ├── api/v1/           # Endpoints (auth, wearables, etc.)
│   │   ├── models/           # Pydantic models
│   │   ├── services/         # Business logic
│   │   └── core/             # Config, database
│   ├── prisma/
│   │   └── schema.prisma     # PostgreSQL schema
│   ├── docker-compose.yml
│   └── requirements.txt
│
├── docs/                      # Documentation
│   ├── BACKEND_API.md        # Complete API reference
│   ├── ANDROID_APP.md        # Android app guide
│   ├── IOS_APP.md            # iOS app guide
│   └── DATABASE_SCHEMA.md    # Database documentation
│
└── README.md                  # This file
```

---

## Development

### Android
```bash
# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Code style
./gradlew ktlintFormat
```

### Backend
```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f api

# Run migrations
docker-compose exec api prisma migrate deploy

# Access Swagger docs
open http://localhost:8000/docs
```

### iOS
```bash
# Open in Xcode
open CloudSync.xcodeproj

# Build & run
# Select device/simulator → Cmd+R
```

---

## Roadmap

- ✅ **Phase 1**: Core features (auth, profiles, UI) - COMPLETE
- ✅ **Phase 2**: Backend integration (FastAPI, databases) - COMPLETE
- ✅ **Phase 3**: Wearable integration (Apple Health, 30K+ metrics) - COMPLETE
- 🚧 **Phase 4**: Real-time Apple Watch sync (background uploads)
- ⏳ **Phase 5**: AI-powered health insights (TimeGPT)
- ⏳ **Phase 6**: Google Fit, Fitbit, Xiaomi Mi Band integration
- ⏳ **Phase 7**: ABDM (Ayushman Bharat) integration
- ⏳ **Phase 8**: Telemedicine features

---

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

---

## License

MIT License - see LICENSE file for details

---

## Contact

- **Repository**: https://github.com/Priyo13o4/MAD_CloudCare
- **Backend API**: https://cloudcare.pipfactor.com/api/v1
- **Swagger Docs**: https://cloudcare.pipfactor.com/docs

---

## Acknowledgments

- Material Design 3 by Google
- Jetpack Compose team
- FastAPI framework
- HealthKit by Apple
- Open source community

---

**Made with ❤️ for better healthcare in India**

*Last Updated: November 2025 | Version 1.0*
