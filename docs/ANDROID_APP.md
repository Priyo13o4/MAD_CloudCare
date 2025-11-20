# CloudCare Android App Documentation

**Version:** 1.0  
**Last Updated:** November 2025  
**Min SDK:** 26 (Android 8.0)  
**Target SDK:** 34 (Android 14)

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Features](#features)
- [App Structure](#app-structure)
- [API Integration](#api-integration)
- [Data Models](#data-models)
- [Setup & Build](#setup--build)

---

## Overview

CloudCare Android app is a comprehensive healthcare management platform for **patients**, **doctors**, and **hospitals**. Built with Jetpack Compose and following MVVM architecture, the app provides:

- **For Patients**: Health tracking, medical records, device pairing, consent management
- **For Doctors**: Patient management, emergency monitoring, appointments, health data access
- **For Hospitals**: Dashboard, staff management, resource tracking, admissions

**Key Features:**
- Material Design 3 UI
- Wearable device integration (Apple Watch via QR pairing)
- Real-time health metrics (30K+ data points tested)
- Offline caching with disk persistence
- IST timezone conversion
- JWT authentication with role-based UI

---

## Architecture

### MVVM Pattern

```
┌─────────────────────────────────────────┐
│           UI Layer (Compose)            │
│  DashboardScreen, WearablesScreen, etc. │
└──────────────┬──────────────────────────┘
               │ observes StateFlow/LiveData
┌──────────────▼──────────────────────────┐
│         ViewModel Layer                 │
│  WearablesViewModel, AuthViewModel      │
└──────────────┬──────────────────────────┘
               │ calls
┌──────────────▼──────────────────────────┐
│         Repository Layer                │
│  HealthMetricsRepository, AuthRepository│
└──────────────┬──────────────────────────┘
               │ uses
┌──────────────▼──────────────────────────┐
│          Data Layer                     │
│  CloudCareApiService (Retrofit)         │
│  Local Cache (SharedPreferences + Files)│
└─────────────────────────────────────────┘
```

### Package Structure

```
com.example.cloudcareapp/
├── data/
│   ├── model/              # Data classes (Patient, Doctor, HealthMetric)
│   ├── remote/             # API services (Retrofit)
│   ├── repository/         # Data abstraction layer
│   └── cache/              # Caching utilities
├── ui/
│   ├── screens/            # Composable screens
│   │   ├── dashboard/      # Patient dashboard
│   │   ├── wearables/      # Health metrics & devices
│   │   ├── records/        # Medical records
│   │   ├── consents/       # Consent management
│   │   ├── facilities/     # Linked facilities
│   │   ├── doctor/         # Doctor screens
│   │   └── hospital/       # Hospital screens
│   ├── viewmodel/          # ViewModels
│   ├── components/         # Reusable UI components
│   ├── navigation/         # Navigation setup
│   └── theme/              # Material 3 theme
└── utils/                  # Utilities (TimeFormatter, etc.)
```

---

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Kotlin | 2.0.21 |
| UI Framework | Jetpack Compose | BOM 2024.09.00 |
| Architecture | MVVM | - |
| Navigation | Navigation Compose | 2.8.2 |
| Material Design | Material 3 | 1.3.0 |
| Networking | Retrofit | 2.9.0 |
| JSON | Gson | 2.9.0 |
| Image Loading | Coil | 2.7.0 |
| Async | Kotlin Coroutines | 1.7.3 |
| Gradle | Gradle | 8.13.0 |
| Build Tool | Android Gradle Plugin | 8.7.3 |

---

## Features

### Patient Features

#### 1. Dashboard
- **Quick Stats**: Linked facilities, medical records, active consents, connected devices
- **Recent Activity Feed**: Data syncs, consent requests, record uploads
- **Quick Actions**: Upload document, request consent, pair device
- **Health Summary**: Steps, heart rate, sleep (today's data)

#### 2. Wearables & Health Tracking
- **Device Management**:
  - View connected devices (Apple Watch, etc.)
  - Device battery level, last sync time
  - QR code pairing for Apple Watch
  - Unpair devices
  
- **Health Metrics Cards**:
  - **Steps**: Daily, weekly, monthly trends with charts
  - **Calories**: Energy expenditure tracking
  - **Heart Rate**: Min, max, average with trends
  - **Sleep**: Duration, stages (awake, REM, core, deep)
  - **Distance**: Walking/running distance
  - **Flights Climbed**: Elevation tracking

- **Time-based Views**:
  - **D (Daily)**: Hourly granularity (24 data points)
  - **W (Weekly)**: Daily granularity (7 data points)
  - **M (Monthly)**: Daily granularity (30 data points)

- **Comprehensive Endpoint**: Single API call fetches all metrics (73% faster)

#### 3. Medical Records
- **Upload Documents**: PDF, images (lab reports, prescriptions, X-rays)
- **Filter by Type**: Lab reports, prescriptions, consultations, imaging, general
- **View & Download**: Open documents, share with doctors
- **Metadata**: Title, date, facility, document type

#### 4. Consent Management
- **4 Tabs**:
  - **Pending**: Awaiting patient approval
  - **Approved**: Active consents (doctor has access)
  - **Denied**: Rejected requests
  - **Revoked**: Previously approved but revoked

- **Actions**:
  - Approve consent (grants doctor full access)
  - Deny consent (doctor remains locked out)
  - Revoke consent (removes doctor access)

- **Info Displayed**: Doctor name, facility, request date, status

#### 5. Linked Facilities
- **Facility Cards**: Hospital/clinic name, type, address
- **Patient IDs**: Internal patient ID at each facility
- **Link Status**: Active, pending, inactive
- **Document Requests**: Request records from facilities

#### 6. Profile Management
- **Personal Info**: Name, DOB, gender, blood group, phone, email
- **Address**: Full residential address
- **Emergency Contact**: Name, phone, relationship
- **Medical History**: Allergies, chronic conditions, medications
- **Insurance**: Provider, policy number, validity

### Doctor Features

#### 1. Doctor Dashboard
- **Patient Overview**: Total patients, critical, monitoring, stable
- **Today's Appointments**: Schedule with patient names, times
- **Emergency Alerts**: Real-time critical patient notifications
- **Quick Stats**: Assigned patients, appointments, emergency cases

#### 2. Patient Management
- **My Patients Screen**:
  - **Active Tab**: Patients with approved consent
  - **Previous Tab**: Revoked/denied patients
  
- **Patient Cards**:
  - Name, condition, status (ACTIVE/LOCKED/STABLE/MONITORING/CRITICAL)
  - Emergency flag, next appointment
  - **View Details** button (enabled for ACTIVE patients)
  - **Remove Patient** option (revokes consent)

- **Patient Detail Screen** (ACTIVE patients only):
  - Personal information (age, gender, blood group, phone)
  - Emergency contact
  - Medical history (allergies, conditions, medications)
  - Insurance details
  - Health metrics (real-time from wearables)
  - Medical records

- **Access Control**:
  - **LOCKED**: Name only, no details
  - **ACTIVE**: Full access to all patient data

#### 3. Emergency Monitoring
- **Alert Feed**: Real-time critical health alerts
- **Severity Levels**: CRITICAL, HIGH, MEDIUM, LOW (color-coded)
- **Alert Types**: Heart rate, oxygen level, blood pressure, temperature
- **Patient Details**: Current values, timestamp, status

#### 4. Appointments
- **Daily Schedule**: List of today's appointments
- **Patient Info**: Name, time, department, reason
- **Status**: Scheduled, in progress, completed, cancelled
- **Add Notes**: Post-consultation notes

#### 5. Profile
- **Professional Info**: License number, specialization, qualifications
- **Hospital Association**: Linked hospital, department
- **Experience**: Years of practice
- **Consultation Fee**: Fee amount
- **Contact**: Phone, email

### Hospital Features

#### 1. Hospital Dashboard
- **Key Metrics**:
  - Total beds vs occupied beds (percentage)
  - ICU beds availability
  - Emergency beds status
  - Operation theatres in use
  
- **Emergency Cases**: Count of critical, high, medium, low severity
- **Response Time**: Average emergency response time
- **Today's Admissions**: New patients admitted today

#### 2. Staff Management
- **Doctor List**: All affiliated doctors
- **Doctor Cards**:
  - Name, specialization, department
  - Experience, assigned patients
  - Availability status
  
#### 3. Resource Management
- **Resource Categories**: Beds, equipment, supplies, medication
- **Resource Cards**:
  - Total vs available count
  - In-use count
  - Status: Normal, Low, Critical (color-coded)
  
- **Resources Tracked**:
  - General beds
  - ICU beds
  - Oxygen cylinders
  - Ventilators
  - Ambulances
  - Blood bags

#### 4. Admissions
- **Admitted Patients**: List of currently admitted patients
- **Patient Cards**:
  - Name, condition, severity, department
  - Admitted time, assigned doctor
  - Status: In treatment, stable, waiting, discharged
  
- **Actions**:
  - View patient details
  - Discharge patient

#### 5. Profile
- **Hospital Info**: Name, type, accreditation
- **Contact**: Phone (primary, emergency), email, website
- **Address**: Full address with city, state
- **Services**: Emergency, ambulance, pharmacy, lab, blood bank
- **Capacity**: Total beds, ICU, emergency, operation theatres

---

## App Structure

### Navigation

**Patient Flow:**
```
Splash → Login/Signup → Dashboard → [Wearables, Records, Consents, Facilities, Profile]
```

**Doctor Flow:**
```
Splash → Login → Doctor Dashboard → [My Patients, Emergency, Appointments, Profile]
  └─ Patient Detail Screen (for ACTIVE patients)
```

**Hospital Flow:**
```
Splash → Login → Hospital Dashboard → [Staff, Resources, Admissions, Profile]
```

### Key Screens

| Screen | Purpose | Access |
|--------|---------|--------|
| `SplashScreen` | App logo, auto-login check | All |
| `DashboardScreen` | Patient main screen | Patient |
| `WearablesScreen` | Health metrics & devices | Patient |
| `MedicalRecordsScreen` | Document management | Patient |
| `ConsentsScreen` | Consent management | Patient |
| `LinkedFacilitiesScreen` | Facility linking | Patient |
| `PatientProfileScreen` | Patient profile edit | Patient |
| `DoctorDashboardScreen` | Doctor main screen | Doctor |
| `MyPatientsScreen` | Assigned patients | Doctor |
| `PatientDetailScreen` | Full patient data | Doctor |
| `EmergencyMonitoringScreen` | Critical alerts | Doctor |
| `DoctorAppointmentsScreen` | Schedule | Doctor |
| `DoctorProfileScreen` | Doctor profile | Doctor |
| `HospitalDashboardScreen` | Hospital metrics | Hospital |
| `HospitalStaffScreen` | Doctor list | Hospital |
| `HospitalResourcesScreen` | Resource tracking | Hospital |
| `HospitalAdmissionsScreen` | Admitted patients | Hospital |
| `HospitalProfileScreen` | Hospital profile | Hospital |

---

## API Integration

### Base Configuration

```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://cloudcare.pipfactor.com/api/v1/"
    
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }
}
```

### API Services

#### 1. CloudCareApiService (Main API)
```kotlin
interface CloudCareApiService {
    // Wearables
    @GET("wearables/metrics/comprehensive")
    suspend fun getComprehensiveMetrics(
        @Query("patient_id") patientId: String,
        @Query("days") days: Int = 30
    ): ComprehensiveMetricsResponse
    
    @GET("wearables/summary/today")
    suspend fun getTodaySummary(
        @Query("patient_id") patientId: String
    ): HealthSummaryResponse
    
    @POST("wearables/devices")
    suspend fun registerDevice(@Body device: WearableDevice): Response<WearableDevice>
    
    // Medical Records
    @GET("documents/{patient_id}")
    suspend fun getMedicalRecords(
        @Path("patient_id") patientId: String
    ): Response<List<MedicalRecord>>
    
    // Consents
    @GET("consents/patient/{patient_id}")
    suspend fun getConsents(
        @Path("patient_id") patientId: String,
        @Query("status_filter") status: String? = null
    ): Response<List<Consent>>
    
    @PATCH("consents/{consent_id}")
    suspend fun updateConsentStatus(
        @Path("consent_id") consentId: String,
        @Body request: UpdateConsentRequest
    ): Response<Consent>
    
    // Doctor
    @GET("doctors/{doctor_id}/patients")
    suspend fun getDoctorPatients(
        @Path("doctor_id") doctorId: String
    ): Response<List<DoctorPatient>>
    
    // Hospital
    @GET("hospitals/{hospital_id}/dashboard")
    suspend fun getHospitalDashboard(
        @Path("hospital_id") hospitalId: String
    ): Response<HospitalDashboardStats>
}
```

#### 2. AuthApiService (Authentication)
```kotlin
interface AuthApiService {
    @POST("auth/signup/patient")
    suspend fun registerPatient(@Body request: RegisterPatientRequest): Response<TokenResponse>
    
    @POST("auth/signup/doctor")
    suspend fun registerDoctor(@Body request: RegisterDoctorRequest): Response<TokenResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>
    
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<TokenResponse>
    
    @GET("auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<AuthUserResponse>
}
```

### Repositories

#### HealthMetricsRepository
```kotlin
class HealthMetricsRepository(
    private val apiService: CloudCareApiService
) {
    suspend fun getComprehensiveMetrics(
        patientId: String,
        days: Int = 30
    ): Result<ComprehensiveMetricsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getComprehensiveMetrics(patientId, days)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTodaySummary(patientId: String): Result<HealthSummaryResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTodaySummary(patientId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### AuthRepository
```kotlin
class AuthRepository(
    private val authApiService: AuthApiService,
    private val context: Context
) {
    suspend fun login(email: String, password: String): Result<TokenResponse> {
        return try {
            val request = LoginRequest(email, password)
            val response = authApiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                saveSession(response.body()!!)
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun saveSession(tokenResponse: TokenResponse) {
        val prefs = context.getSharedPreferences("cloudcare_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("access_token", tokenResponse.access_token)
            putString("refresh_token", tokenResponse.refresh_token)
            putString("user_id", tokenResponse.user.id)
            putString("user_role", tokenResponse.user.role)
            apply()
        }
    }
}
```

---

## Data Models

### Core Models

```kotlin
// Patient
data class Patient(
    val id: String,
    val first_name: String,
    val last_name: String,
    val date_of_birth: String,
    val gender: String,
    val blood_group: String?,
    val phone_primary: String,
    val email: String,
    val aadhar_uid: String,
    val allergies: List<String>?,
    val chronic_conditions: List<String>?,
    val current_medications: List<String>?
)

// Health Metric
data class HealthMetric(
    val id: String,
    val patient_id: String,
    val metric_type: String,
    val value: Double,
    val unit: String,
    val timestamp: String
)

// Comprehensive Response
data class ComprehensiveMetricsResponse(
    val patient_id: String,
    val request_timestamp: String,
    val summary: Map<String, MetricSummary>,
    val time_series: TimeSeriesData,
    val device_info: DeviceInfo
)

data class TimeSeriesData(
    val steps: List<TimeSeriesDataPoint>,
    val steps_hourly: List<TimeSeriesDataPoint>,
    val calories: List<TimeSeriesDataPoint>,
    val calories_hourly: List<TimeSeriesDataPoint>,
    val heart_rate: List<TimeSeriesDataPoint>,
    val heart_rate_hourly: List<TimeSeriesDataPoint>,
    val distance: List<TimeSeriesDataPoint>,
    val distance_hourly: List<TimeSeriesDataPoint>,
    val sleep: List<TimeSeriesDataPoint>
)

// Medical Record
data class MedicalRecord(
    val id: String,
    val patient_id: String,
    val title: String,
    val record_type: RecordType,
    val date: String,
    val file_url: String?,
    val facility_name: String?
)

enum class RecordType {
    LAB_REPORT,
    PRESCRIPTION,
    GENERAL,
    CONSULTATION,
    IMAGING
}

// Consent
data class Consent(
    val id: String,
    val patient_id: String,
    val facility_name: String,
    val request_type: String,
    val status: ConsentStatus,
    val requested_at: String
)

enum class ConsentStatus {
    PENDING,
    APPROVED,
    DENIED,
    REVOKED
}

// Doctor Patient
data class DoctorPatient(
    val patient_id: String,
    val patient_name: String,
    val status: PatientStatus,
    val condition: String,
    val next_appointment: String?,
    val emergency_flag: Boolean
)

enum class PatientStatus {
    STABLE,
    MONITORING,
    CRITICAL,
    LOCKED,
    ACTIVE
}

// Emergency Alert
data class EmergencyAlert(
    val id: String,
    val patient_id: String,
    val patient_name: String,
    val severity: AlertSeverity,
    val alert_type: AlertType,
    val message: String,
    val timestamp: String
)

enum class AlertSeverity { CRITICAL, HIGH, MEDIUM, LOW }
enum class AlertType { HEART_RATE, OXYGEN_LEVEL, BLOOD_PRESSURE, TEMPERATURE, OTHER }
```

---

## Setup & Build

### Prerequisites
- Android Studio Ladybug or later
- JDK 11 or later
- Android SDK API 26+ (target 34)
- Gradle 8.13.0+

### Clone & Build

```bash
# 1. Clone repository
git clone https://github.com/Priyo13o4/MAD_CloudCare.git
cd MAD_CloudCare/CloudCare\ Android\ App

# 2. Open in Android Studio
# File → Open → Select "CloudCare Android App" folder

# 3. Build APK
./gradlew assembleDebug

# 4. Install on device
./gradlew installDebug

# 5. Build release APK
./gradlew assembleRelease
```

### Configuration

**Backend URL** (`RetrofitClient.kt`):
```kotlin
private const val BASE_URL = "https://cloudcare.pipfactor.com/api/v1/"
```

**Dependencies** (`app/build.gradle.kts`):
```kotlin
dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.2")
    
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Coil
    implementation("io.coil-kt:coil-compose:2.7.0")
}
```

### Run Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

---

## Key Features Implementation

### 1. Caching Strategy

**Cache-First Architecture:**
```kotlin
class WearablesViewModel(
    private val healthMetricsRepository: HealthMetricsRepository
) : ViewModel() {
    
    init {
        // 1. Load from cache first
        loadCacheFromDisk()
        
        // 2. Fetch fresh data in background
        loadComprehensiveMetrics()
    }
    
    fun refresh() {
        viewModelScope.launch {
            // Manual sync
            val result = healthMetricsRepository.getComprehensiveMetrics(patientId)
            result.onSuccess { response ->
                // Update cache
                updateCache(response)
                saveCacheToDisk()
            }
        }
    }
}
```

### 2. IST Timezone Conversion

**TimeFormatter Utility:**
```kotlin
object TimeFormatter {
    private val istZone = ZoneId.of("Asia/Kolkata") // UTC+5:30
    
    fun parseUtcToIst(isoTimestamp: String?): String {
        val instant = Instant.parse(isoTimestamp)
        val istTime = instant.atZone(istZone)
        return istTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
    }
    
    fun getRelativeTime(isoTimestamp: String?): String {
        val instant = Instant.parse(isoTimestamp)
        val duration = Duration.between(instant, Instant.now())
        return when {
            duration.toMinutes() < 1 -> "Just now"
            duration.toMinutes() < 60 -> "${duration.toMinutes()} minutes ago"
            duration.toHours() < 24 -> "${duration.toHours()} hours ago"
            else -> "${duration.toDays()} days ago"
        }
    }
}
```

### 3. Error Handling

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// In ViewModel
private val _uiState = MutableStateFlow<UiState<ComprehensiveMetricsResponse>>(UiState.Loading)
val uiState: StateFlow<UiState<ComprehensiveMetricsResponse>> = _uiState

fun loadData() {
    viewModelScope.launch {
        _uiState.value = UiState.Loading
        val result = repository.getComprehensiveMetrics(patientId)
        _uiState.value = result.fold(
            onSuccess = { UiState.Success(it) },
            onFailure = { UiState.Error(it.message ?: "Unknown error") }
        )
    }
}
```

---

## Performance

### Optimizations
- **Comprehensive Endpoint**: 73% faster (1 call replaces 5+)
- **Lazy Composables**: `LazyColumn`, `LazyRow` for scrolling lists
- **State Hoisting**: Minimal recompositions
- **Disk Caching**: Offline support, instant load
- **Image Loading**: Coil with memory/disk caching
- **Coroutines**: Non-blocking async operations

### Memory Management
- `remember` for state across recompositions
- `derivedStateOf` for computed values
- `DisposableEffect` for cleanup
- ViewModel survives configuration changes

---

## Testing

### Test Patient Data
- **Patient ID**: `3228128A-7110-4D47-8EDB-3A9160E3808A`
- **Health Metrics**: 30,186 documents in MongoDB
- **Device**: Apple Watch Series 9
- **Last Sync**: 2025-11-19 09:00:13 UTC (14:30 IST)

### Test Scenarios
1. Login as patient → Dashboard loads
2. Navigate to Wearables → Health metrics display
3. Tap Daily (D) → Hourly data shows
4. Pull to refresh → Data updates
5. Navigate to Consents → Pending/Approved tabs work
6. Approve consent → Status changes
7. Login as doctor → My Patients loads
8. Tap patient (ACTIVE) → View Details opens
9. Login as hospital → Dashboard stats load

---

## Summary

CloudCare Android app is a production-ready healthcare management platform with:
- ✅ **MVVM architecture** with Jetpack Compose
- ✅ **Material Design 3** UI
- ✅ **Wearable integration** (Apple Watch pairing, 30K+ metrics)
- ✅ **Comprehensive API** (single call, 73% faster)
- ✅ **Offline caching** (disk persistence)
- ✅ **IST timezone** (automatic UTC conversion)
- ✅ **Role-based UI** (patient, doctor, hospital)
- ✅ **Real-time updates** (health alerts, consent status)

**Min SDK:** 26 (Android 8.0)  
**Target SDK:** 34 (Android 14)  
**Backend:** https://cloudcare.pipfactor.com/api/v1

---

*Last Updated: November 2025 | Version 1.0*
