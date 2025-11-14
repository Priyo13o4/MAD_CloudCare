# CloudCare - Complete Application Documentation

**Version:** 1.0  
**Platform:** Android (Jetpack Compose)  
**Minimum SDK:** 26 (Android 8.0)  
**Target SDK:** 36  
**Build System:** Gradle with Kotlin DSL

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Features](#features)
4. [Technology Stack](#technology-stack)
5. [Application Structure](#application-structure)
6. [Data Models](#data-models)
7. [User Flows](#user-flows)
8. [UI Components](#ui-components)
9. [Navigation](#navigation)
10. [State Management](#state-management)

---

## Project Overview

CloudCare is a comprehensive healthcare management Android application designed to facilitate seamless interaction between patients, doctors, and hospitals. The application serves as a centralized platform for:

- **Patient Health Management**: Track health metrics from wearable devices, manage medical records, and control data access
- **Doctor-Patient Interaction**: Enable doctors to monitor patients, manage appointments, and handle emergency alerts
- **Hospital Administration**: Manage staff, resources, admissions, and emergency cases
- **Data Privacy & Consent**: Patient-controlled consent system for sharing medical data with healthcare providers

### Key Value Propositions

1. **Patient-Centric Data Control**: Patients own and control their health data
2. **Real-time Health Monitoring**: Integration with wearable devices for continuous health tracking
3. **Seamless Healthcare Coordination**: Connect patients, doctors, and hospitals on a single platform
4. **Privacy-First Design**: Explicit consent system for all data sharing
5. **Emergency Response**: Real-time alerts and critical patient monitoring

---

## Architecture

### Design Pattern

CloudCare follows the **MVVM (Model-View-ViewModel)** architectural pattern with the following benefits:

- **Separation of Concerns**: UI logic separated from business logic
- **Testability**: ViewModels can be unit tested independently
- **Lifecycle Awareness**: ViewModels survive configuration changes
- **Reactive Data Flow**: Using Kotlin Flows for state management

### Architecture Layers

```
┌─────────────────────────────────────┐
│         UI Layer (Compose)          │
│  - Screens                          │
│  - Components                       │
│  - Navigation                       │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│       ViewModel Layer               │
│  - State Management                 │
│  - Business Logic                   │
│  - Data Transformation              │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Repository Layer               │
│  - Data Source Abstraction          │
│  - API/Local Data Management        │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Data Layer                  │
│  - Models                           │
│  - Data Sources                     │
│  - Mock Data (current)              │
└─────────────────────────────────────┘
```

---

## Features

### Patient Features

#### 1. Dashboard
- Welcome card with patient name
- Quick stats overview (linked facilities, health records, pending consents, connected devices)
- Recent activity feed
- Quick action buttons for common tasks

#### 2. Wearables & Health Tracking
- **Connected Devices Management**
  - View connected wearable devices (fitness trackers, smartwatches)
  - Device status (battery level, connection status)
  - Last sync time and data points synced
  
- **Health Metrics Display**
  - Steps tracking with daily goals
  - Heart rate monitoring
  - Sleep tracking
  - Calorie burn tracking
  - Visual circular progress indicators
  
- **Health Insights**
  - Weekly trends and averages
  - AI-powered health recommendations
  - Comparative analysis (today vs. week average)
  
- **Data Visualization**
  - Heart rate charts
  - Activity graphs
  - Progress tracking

#### 3. Medical Records
- View all medical records
- Filter by record type (Lab Reports, Prescriptions, Consultations, Imaging)
- Search functionality
- Upload new records
- Share records with healthcare providers

#### 4. Scan & Share
- QR code generation for quick data sharing
- Scan facility QR codes to link accounts
- Temporary access code generation
- Emergency data access

#### 5. Consent Management
- **Pending Requests**
  - View consent requests from healthcare facilities
  - Detailed information about requested data
  - Approve/Deny actions
  
- **Approved Consents**
  - List of active consents
  - Revoke access capability
  - Consent history

#### 6. Linked Facilities
- View all connected healthcare facilities
- Facility types: Hospitals, Clinics, Labs, Pharmacies
- Patient ID for each facility
- Quick actions (view records, contact)

#### 7. Profile Management
- Personal information display
- Emergency contacts
- Insurance information
- Account settings
- Privacy preferences

### Doctor Features

#### 1. Doctor Dashboard
- Statistics overview (total patients, appointments, alerts, pending reports)
- Quick access to patient management
- Emergency alert feed
- Schedule overview

#### 2. Patient Management
- View assigned patients list
- Patient status indicators (Stable, Monitoring, Critical)
- Filter by status and emergency flags
- Quick access to patient details
- Next appointment information

#### 3. Emergency Monitoring
- Real-time emergency alerts
- Severity levels (Critical, High, Medium, Low)
- Alert types (Heart Rate, Oxygen Level, Blood Pressure, Temperature)
- Quick response actions
- Patient vital signs display

#### 4. Appointment Schedule
- Daily appointment view
- Appointment status tracking
- Patient information preview
- Notes and reason for visit
- Mark as completed/cancelled

#### 5. Patient Records Access
- View patient medical history (with consent)
- Add consultation notes
- Prescribe medications
- Order tests
- View test results

### Hospital Features

#### 1. Hospital Dashboard
- Key metrics (admitted patients, available doctors, emergency cases)
- Response time tracking
- Bed availability overview
- Department status

#### 2. Staff Management
- View all hospital staff
- Staff specializations and departments
- Current patient assignments
- Availability status
- Contact information

#### 3. Resource Management
- **Bed Management**
  - Total vs. available beds
  - Department-wise allocation
  - Real-time availability updates

- **Equipment Tracking**
  - Equipment inventory
  - Usage status
  - Maintenance schedules

- **Supply Management**
  - Medical supplies inventory
  - Stock levels and alerts
  - Low-stock notifications

#### 4. Admissions
- View all admitted patients
- Emergency admission tracking
- Department assignments
- Assigned doctor information
- Patient condition monitoring

---

## Technology Stack

### Frontend (Android)

| Component | Technology | Version |
|-----------|-----------|---------|
| UI Framework | Jetpack Compose | BOM 2024.09.00 |
| Language | Kotlin | 2.0.21 |
| Architecture Components | Lifecycle, ViewModel | 2.8.6 |
| Navigation | Navigation Compose | 2.8.2 |
| Material Design | Material 3 | 1.3.0 |
| Image Loading | Coil | 2.7.0 |
| System UI | Accompanist | 0.34.0 |

### Build Tools

- **Gradle**: 8.13.0
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 36
- **Compile SDK**: 36
- **Java Version**: 11

### Design System

- **Material Design 3**: Modern Android design system
- **Custom Color Theme**: Branded color palette
- **Typography**: Material 3 type scale
- **Icons**: Material Icons Extended

---

## Application Structure

### Package Organization

```
com.example.cloudcareapp/
├── MainActivity.kt                    # Main activity with navigation
├── CloudCareApplication.kt           # Application class
│
├── data/
│   ├── model/                        # Data models
│   │   ├── Patient.kt               # Patient & wearable models
│   │   ├── DoctorModels.kt          # Doctor-related models
│   │   ├── HospitalModels.kt        # Hospital-related models
│   │   └── Facility.kt              # Facility & consent models
│   │
│   ├── repository/                   # Data repositories
│   │   └── MockDataRepository.kt    # Mock data provider
│   │
│   └── MockData.kt files            # Mock data generators
│
├── navigation/
│   └── Screen.kt                    # Navigation routes & bottom nav
│
├── ui/
│   ├── theme/                       # App theming
│   │   ├── Color.kt                # Color definitions
│   │   ├── Type.kt                 # Typography
│   │   └── Theme.kt                # Theme configuration
│   │
│   └── screens/                     # Feature screens
│       ├── splash/                  # Splash screen
│       ├── auth/                    # Login screens
│       ├── dashboard/               # Patient dashboard
│       ├── wearables/               # Wearable devices & health
│       ├── records/                 # Medical records
│       ├── scanshare/               # QR scanning & sharing
│       ├── consents/                # Consent management
│       ├── facilities/              # Linked facilities
│       ├── profile/                 # User profile
│       ├── notifications/           # Notifications
│       ├── doctor/                  # Doctor screens
│       ├── hospital/                # Hospital screens
│       ├── settings/                # Settings
│       ├── help/                    # Help & support
│       ├── about/                   # About screen
│       └── privacy/                 # Privacy & security
```

---

## Data Models

### Patient Models

#### Patient
```kotlin
data class Patient(
    val id: Int,
    val name: String,
    val age: Int,
    val gender: String,
    val bloodType: String,
    val contact: String,
    val email: String,
    val address: String,
    val familyContact: String,
    val insuranceProvider: String,
    val insuranceId: String,
    val emergency: Boolean = false,
    val occupation: String = "",
    val aiAnalysis: String? = null
)
```

#### WearableDevice
```kotlin
data class WearableDevice(
    val id: Int,
    val name: String,
    val type: String,
    val isConnected: Boolean,
    val batteryLevel: Int,
    val lastSyncTime: String,
    val dataPointsSynced: Int,
    val iconType: DeviceIconType
)
```

#### HealthSummary
```kotlin
data class HealthSummary(
    val steps: Int,
    val stepsChange: Int,          // percentage
    val heartRate: Int,
    val heartRateStatus: String,    // "Normal", "Elevated"
    val sleepHours: Double,
    val sleepChange: Int,           // percentage
    val calories: Int,
    val caloriesPercentage: Int,    // of goal
    val caloriesGoal: Int = 2000
)
```

#### WearableData
```kotlin
data class WearableData(
    val id: Int,
    val timestamp: String,
    val heartRate: Int?,
    val steps: Int?,
    val sleepHours: Double?,
    val calories: Int?,
    val oxygenLevel: Int?,
    val bloodPressureSystolic: Int?,
    val bloodPressureDiastolic: Int?
)
```

### Consent & Facility Models

#### Consent
```kotlin
data class Consent(
    val id: Int,
    val facilityName: String,
    val requestType: String,
    val timestamp: String,
    val status: ConsentStatus,
    val description: String = ""
)

enum class ConsentStatus {
    PENDING, APPROVED, DENIED
}
```

#### Facility
```kotlin
data class Facility(
    val id: Int,
    val name: String,
    val type: FacilityType,
    val patientId: String,
    val iconType: FacilityIconType
)

enum class FacilityType {
    HOSPITAL, CLINIC, LAB, PHARMACY
}
```

### Doctor Models

#### EmergencyAlert
```kotlin
data class EmergencyAlert(
    val id: String,
    val patientId: String,
    val patientName: String,
    val severity: AlertSeverity,
    val alertType: AlertType,
    val message: String,
    val timestamp: String,
    val currentValue: String = ""
)

enum class AlertSeverity {
    CRITICAL, HIGH, MEDIUM, LOW
}
```

#### AssignedPatient
```kotlin
data class AssignedPatient(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val status: PatientStatus,
    val condition: String,
    val nextAppointment: String,
    val lastVisit: String,
    val emergencyFlag: Boolean = false
)
```

### Hospital Models

#### HospitalStats
```kotlin
data class HospitalStats(
    val admittedPatients: Int,
    val availableDoctors: Int,
    val emergencyCases: Int,
    val avgResponseTime: String,
    val totalBeds: Int,
    val availableBeds: Int
)
```

---

## User Flows

### Patient Journey

1. **App Launch** → Splash Screen (2s)
2. **Login Selection** → Choose Patient Login
3. **Authentication** → Enter credentials
4. **Dashboard** → View health overview
5. **Connect Wearable** → Link fitness tracker
6. **View Health Data** → Monitor vitals
7. **Consent Request** → Approve/Deny facility access
8. **Share Records** → Generate QR code for doctor

### Doctor Journey

1. **App Launch** → Splash Screen
2. **Login Selection** → Choose Doctor Login
3. **Authentication** → Enter credentials
4. **Doctor Dashboard** → View assigned patients
5. **Emergency Alert** → Receive critical patient alert
6. **Patient Details** → View vitals and history
7. **Add Notes** → Document consultation
8. **Prescribe** → Add prescription

### Hospital Journey

1. **App Launch** → Splash Screen
2. **Login Selection** → Choose Hospital Login
3. **Authentication** → Enter credentials
4. **Hospital Dashboard** → View facility stats
5. **Manage Admissions** → Process new patient
6. **Resource Allocation** → Assign bed and doctor
7. **Monitor Emergency** → Track critical cases

---

## Navigation

### Navigation Structure

```
Splash
  └─> Login Selection
       ├─> Patient Login ──> Patient Dashboard (Bottom Nav)
       │                      ├─> Dashboard
       │                      ├─> Records
       │                      ├─> Scan & Share
       │                      └─> Consents
       │
       ├─> Doctor Login ──> Doctor Dashboard
       │                     ├─> Patients List
       │                     ├─> Emergency Alerts
       │                     ├─> Schedule
       │                     └─> Records
       │
       └─> Hospital Login ──> Hospital Dashboard
                              ├─> Staff Management
                              ├─> Resources
                              └─> Admissions
```

### Bottom Navigation (Patient)

1. **Dashboard**: Home screen with overview
2. **Records**: Medical records list
3. **Scan & Share**: QR code functionality
4. **Consents**: Consent management

### Drawer Navigation (Common)

- Settings
- Help & Support
- About
- Logout

---

## State Management

### ViewModel Pattern

Each screen has a corresponding ViewModel that:
- Manages UI state using StateFlow
- Handles business logic
- Communicates with repository
- Survives configuration changes

### Example: WearablesViewModel

```kotlin
class WearablesViewModel(
    private val repository: MockDataRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<WearablesUiState>(Loading)
    val uiState: StateFlow<WearablesUiState> = _uiState.asStateFlow()
    
    init {
        loadWearablesData()
    }
    
    fun loadWearablesData() {
        viewModelScope.launch {
            try {
                _uiState.value = Loading
                
                val devices = repository.getWearableDevices()
                val healthSummary = repository.getHealthSummary()
                val insights = repository.getHealthInsights()
                
                _uiState.value = Success(devices, healthSummary, insights)
            } catch (e: Exception) {
                _uiState.value = Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class WearablesUiState {
    object Loading : WearablesUiState()
    data class Success(...) : WearablesUiState()
    data class Error(val message: String) : WearablesUiState()
}
```

---

## UI Components

### Key Composable Components

#### 1. Cards
- **WelcomeCard**: Gradient card with patient greeting
- **StatsCard**: Metric display with icon and value
- **DeviceCard**: Wearable device status display
- **ConsentCard**: Consent request with actions
- **ActivityItem**: Activity feed item

#### 2. Charts & Visualizations
- **CircularProgressIndicator**: Animated circular progress (calories goal)
- **HeartRateChart**: Line chart for heart rate trends
- **BarChart**: Step count visualization

#### 3. Input Components
- **CustomTextField**: Styled text input
- **CustomButton**: Branded button styles
- **Chip**: Status chips and filters

#### 4. Lists
- **LazyColumn**: Scrollable lists for all data
- **GridLayout**: Stats grid, quick actions

---

## Current Limitations & Future Enhancements

### Current State
- **Mock Data**: All data is currently mocked in `MockDataRepository`
- **No Backend**: No real API integration
- **No Persistence**: No local database
- **No Authentication**: Login is UI-only, no real auth
- **No Wearable Integration**: Device data is simulated

### Required for Production
1. **Backend API Integration** (See BACKEND_SETUP_GUIDE.md)
2. **Real Authentication System**
3. **Wearable SDK Integration** (Google Fit, Apple HealthKit, etc.)
4. **Local Database** (Room)
5. **Push Notifications**
6. **File Upload/Download**
7. **Encryption for Health Data**
8. **Real-time Sync**

---

## Building & Running

### Prerequisites
- Android Studio Ladybug or later
- JDK 11 or later
- Android SDK with API 26+

### Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

### Configuration Files

- `build.gradle.kts`: App-level build configuration
- `settings.gradle.kts`: Project settings
- `gradle/libs.versions.toml`: Dependency version catalog
- `local.properties`: Local SDK paths

---

## Code Quality & Best Practices

### Followed Practices

1. **Kotlin Coding Conventions**
2. **Material Design 3 Guidelines**
3. **Jetpack Compose Best Practices**
4. **MVVM Architecture**
5. **Single Responsibility Principle**
6. **Dependency Injection Ready** (prepared for Hilt)

### Code Organization

- Clear package structure
- Separated concerns (UI, data, navigation)
- Reusable composable components
- Consistent naming conventions

---

## Dependencies

### Core Dependencies
```toml
androidx-core-ktx = "1.15.0"
androidx-lifecycle-runtime-ktx = "2.8.6"
androidx-lifecycle-viewmodel-compose = "2.8.6"
androidx-activity-compose = "1.9.2"
```

### Compose Dependencies
```toml
compose-bom = "2024.09.00"
material3 = "1.3.0"
navigation-compose = "2.8.2"
```

### Additional Libraries
```toml
accompanist-systemuicontroller = "0.34.0"
coil-compose = "2.7.0"
```

---

## Security Considerations

### Implemented
- Material 3 security patterns
- UI-level data protection

### Required for Production
- **Data Encryption**: Encrypt health data at rest
- **Secure Communication**: HTTPS/TLS for all API calls
- **Authentication**: OAuth 2.0 or JWT-based auth
- **Biometric Authentication**: Fingerprint/Face unlock
- **Certificate Pinning**: Prevent MITM attacks
- **Data Sanitization**: Validate all inputs
- **Audit Logs**: Track all data access

---

## Performance Considerations

### Current Optimizations
- Lazy loading with LazyColumn
- State hoisting
- Remember for expensive computations
- Minimal recompositions

### Future Optimizations
- Image caching with Coil
- Background sync for wearable data
- Pagination for large lists
- WorkManager for periodic tasks

---

## Testing Strategy

### Current Testing
- Unit test structure in place
- Instrumented test structure ready

### Recommended Testing
1. **Unit Tests**: ViewModels, repositories, utilities
2. **UI Tests**: Compose UI testing
3. **Integration Tests**: Navigation flows
4. **Snapshot Tests**: UI component screenshots
5. **End-to-End Tests**: Complete user journeys

---

## Deployment

### Build Variants
- **Debug**: Development build with debugging enabled
- **Release**: Production build with ProGuard

### Release Checklist
- [ ] Update version code and name
- [ ] Configure ProGuard rules
- [ ] Sign APK with release keystore
- [ ] Test on multiple devices
- [ ] Prepare app store assets
- [ ] Submit to Google Play Store

---

## Support & Maintenance

### Version Control
- Git repository structure
- Feature branch workflow
- Semantic versioning

### Documentation
- Code comments for complex logic
- README files for each module
- API documentation (when backend added)

---

## License & Credits

**Application Name**: CloudCare  
**Version**: 1.0.0  
**Platform**: Android  
**Framework**: Jetpack Compose  

---

**End of Documentation**
