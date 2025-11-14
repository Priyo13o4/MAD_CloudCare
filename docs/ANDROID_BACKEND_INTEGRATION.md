# Android App - Backend Integration Guide

Quick reference for connecting the CloudCare Android app to the Flask backend.

---

## Overview

This guide shows how to modify the Android app to connect to your Flask backend instead of using mock data.

---

## 1. Add Networking Dependencies

Update `app/build.gradle.kts`:

```kotlin
dependencies {
    // Existing dependencies...
    
    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // DataStore for secure token storage
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Socket.IO for real-time updates (wearables)
    implementation("io.socket:socket.io-client:2.1.0")
}
```

---

## 2. Create API Service Interfaces

Create `data/remote/` package with API definitions:

### `data/remote/ApiService.kt`
```kotlin
package com.example.cloudcareapp.data.remote

import retrofit2.http.*

interface AuthApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
    
    @POST("api/v1/auth/logout")
    suspend fun logout(): ApiResponse
}

interface PatientApiService {
    @GET("api/v1/patient/profile")
    suspend fun getProfile(): PatientProfileResponse
    
    @PUT("api/v1/patient/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse
    
    @GET("api/v1/patient/health-summary")
    suspend fun getHealthSummary(): HealthSummaryResponse
    
    @GET("api/v1/patient/facilities")
    suspend fun getLinkedFacilities(): FacilitiesResponse
}

interface WearablesApiService {
    @POST("api/v1/wearables/data/sync")
    suspend fun syncHealthData(@Body request: WearableDataSyncRequest): SyncResponse
    
    @GET("api/v1/wearables/devices")
    suspend fun getDevices(): DevicesResponse
    
    @POST("api/v1/wearables/device/register")
    suspend fun registerDevice(@Body request: RegisterDeviceRequest): ApiResponse
    
    @GET("api/v1/wearables/data/history")
    suspend fun getHealthHistory(
        @Query("metric_type") metricType: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): HealthHistoryResponse
}

interface ConsentsApiService {
    @GET("api/v1/consents")
    suspend fun getConsents(): ConsentsResponse
    
    @PUT("api/v1/consents/{consent_id}/approve")
    suspend fun approveConsent(@Path("consent_id") consentId: Int): ApiResponse
    
    @PUT("api/v1/consents/{consent_id}/deny")
    suspend fun denyConsent(@Path("consent_id") consentId: Int): ApiResponse
    
    @PUT("api/v1/consents/{consent_id}/revoke")
    suspend fun revokeConsent(@Path("consent_id") consentId: Int): ApiResponse
}

interface DocumentsApiService {
    @POST("api/v1/documents/request")
    suspend fun requestDocument(@Body request: DocumentRequestRequest): ApiResponse
    
    @GET("api/v1/documents/requests")
    suspend fun getDocumentRequests(): DocumentRequestsResponse
    
    @GET("api/v1/records")
    suspend fun getMedicalRecords(): MedicalRecordsResponse
}
```

---

## 3. Create Request/Response Models

### `data/remote/models/AuthModels.kt`
```kotlin
package com.example.cloudcareapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("user_type")
    val userType: String // "patient", "doctor", "hospital"
)

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("user_type")
    val userType: String,
    @SerializedName("patient_uid")
    val patientUid: String? = null
)

data class RegisterRequest(
    @SerializedName("aadhar_number")
    val aadharNumber: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("date_of_birth")
    val dateOfBirth: String,
    val gender: String,
    val phone: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val status: String,
    val message: String,
    @SerializedName("patient_uid")
    val patientUid: String,
    @SerializedName("patient_id")
    val patientId: Int
)
```

### `data/remote/models/WearablesModels.kt`
```kotlin
package com.example.cloudcareapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class WearableDataSyncRequest(
    @SerializedName("patient_aadhar_uid")
    val patientAadharUid: String,
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("data_points")
    val dataPoints: List<DataPointDto>
)

data class DataPointDto(
    @SerializedName("metric_type")
    val metricType: String,
    val value: Number,
    val unit: String,
    val timestamp: String
)

data class SyncResponse(
    val status: String,
    @SerializedName("synced_count")
    val syncedCount: Int,
    @SerializedName("inserted_ids")
    val insertedIds: List<String>
)

data class DevicesResponse(
    val status: String,
    val devices: List<DeviceDto>
)

data class DeviceDto(
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("device_name")
    val deviceName: String,
    @SerializedName("device_type")
    val deviceType: String,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("battery_level")
    val batteryLevel: Int,
    @SerializedName("last_sync")
    val lastSync: String
)
```

---

## 4. Setup Retrofit Client

### `data/remote/RetrofitClient.kt`
```kotlin
package com.example.cloudcareapp.data.remote

import com.example.cloudcareapp.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    // Change this to your backend URL
    private const val BASE_URL = "https://api.cloudcare.com/"
    // For local testing: "http://10.0.2.2:5000/" (Android Emulator)
    // For local testing: "http://YOUR_IP:5000/" (Physical Device)
    
    private var authToken: String? = null
    
    fun setAuthToken(token: String) {
        authToken = token
    }
    
    fun clearAuthToken() {
        authToken = null
    }
    
    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        
        // Add authorization header if token exists
        authToken?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        
        // Add common headers
        requestBuilder.addHeader("Content-Type", "application/json")
        requestBuilder.addHeader("Accept", "application/json")
        
        chain.proceed(requestBuilder.build())
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val authApi: AuthApiService = retrofit.create(AuthApiService::class.java)
    val patientApi: PatientApiService = retrofit.create(PatientApiService::class.java)
    val wearablesApi: WearablesApiService = retrofit.create(WearablesApiService::class.java)
    val consentsApi: ConsentsApiService = retrofit.create(ConsentsApiService::class.java)
    val documentsApi: DocumentsApiService = retrofit.create(DocumentsApiService::class.java)
}
```

---

## 5. Create Real Repository

### `data/repository/CloudCareRepository.kt`
```kotlin
package com.example.cloudcareapp.data.repository

import com.example.cloudcareapp.data.model.*
import com.example.cloudcareapp.data.remote.RetrofitClient
import com.example.cloudcareapp.data.remote.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CloudCareRepository {
    
    private val authApi = RetrofitClient.authApi
    private val patientApi = RetrofitClient.patientApi
    private val wearablesApi = RetrofitClient.wearablesApi
    private val consentsApi = RetrofitClient.consentsApi
    
    // Authentication
    suspend fun login(email: String, password: String, userType: String): Result<LoginResponse> {
        return try {
            val response = authApi.login(LoginRequest(email, password, userType))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun register(
        aadharNumber: String,
        fullName: String,
        dateOfBirth: String,
        gender: String,
        phone: String,
        email: String,
        password: String
    ): Result<RegisterResponse> {
        return try {
            val response = authApi.register(
                RegisterRequest(
                    aadharNumber = aadharNumber,
                    fullName = fullName,
                    dateOfBirth = dateOfBirth,
                    gender = gender,
                    phone = phone,
                    email = email,
                    password = password
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Patient Profile
    suspend fun getPatient(): Result<Patient> {
        return try {
            val response = patientApi.getProfile()
            Result.success(response.toPatient())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Wearables
    suspend fun getWearableDevices(): List<WearableDevice> {
        return try {
            val response = wearablesApi.getDevices()
            response.devices.map { it.toWearableDevice() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun syncWearableData(
        patientUid: String,
        deviceId: String,
        dataPoints: List<DataPointDto>
    ): Result<SyncResponse> {
        return try {
            val response = wearablesApi.syncHealthData(
                WearableDataSyncRequest(patientUid, deviceId, dataPoints)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getHealthSummary(): HealthSummary {
        return try {
            val response = patientApi.getHealthSummary()
            response.toHealthSummary()
        } catch (e: Exception) {
            // Return default on error
            HealthSummary(
                steps = 0,
                stepsChange = 0,
                heartRate = 0,
                heartRateStatus = "Unknown",
                sleepHours = 0.0,
                sleepChange = 0,
                calories = 0,
                caloriesPercentage = 0,
                caloriesGoal = 2000
            )
        }
    }
    
    // Consents
    suspend fun getConsents(): List<Consent> {
        return try {
            val response = consentsApi.getConsents()
            response.consents.map { it.toConsent() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun approveConsent(consentId: Int): Result<Unit> {
        return try {
            consentsApi.approveConsent(consentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun denyConsent(consentId: Int): Result<Unit> {
        return try {
            consentsApi.denyConsent(consentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Flow-based methods for reactive updates
    fun getHealthSummaryFlow(): Flow<HealthSummary> = flow {
        emit(getHealthSummary())
    }
}

// Extension functions to convert DTOs to domain models
private fun PatientProfileResponse.toPatient(): Patient {
    return Patient(
        id = this.id,
        name = this.fullName,
        age = this.age,
        gender = this.gender,
        bloodType = this.bloodType ?: "",
        contact = this.phone,
        email = this.email,
        address = this.address ?: "",
        familyContact = this.emergencyContact ?: "",
        insuranceProvider = this.insuranceProvider ?: "",
        insuranceId = this.insuranceId ?: "",
        emergency = false
    )
}

private fun DeviceDto.toWearableDevice(): WearableDevice {
    return WearableDevice(
        id = this.deviceId.hashCode(),
        name = this.deviceName,
        type = this.deviceType,
        isConnected = this.isActive,
        batteryLevel = this.batteryLevel,
        lastSyncTime = this.lastSync,
        dataPointsSynced = 0,
        iconType = DeviceIconType.FITNESS_TRACKER
    )
}
```

---

## 6. Update ViewModels

Replace mock repository with real repository in ViewModels:

### `ui/screens/wearables/WearablesViewModel.kt`
```kotlin
package com.example.cloudcareapp.ui.screens.wearables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudcareapp.data.repository.CloudCareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WearablesViewModel(
    private val repository: CloudCareRepository = CloudCareRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<WearablesUiState>(WearablesUiState.Loading)
    val uiState: StateFlow<WearablesUiState> = _uiState.asStateFlow()
    
    init {
        loadWearablesData()
    }
    
    fun loadWearablesData() {
        viewModelScope.launch {
            try {
                _uiState.value = WearablesUiState.Loading
                
                // Fetch from backend instead of mock data
                val devices = repository.getWearableDevices()
                val healthSummary = repository.getHealthSummary()
                val insights = repository.getHealthInsights()
                
                _uiState.value = WearablesUiState.Success(
                    devices = devices,
                    healthSummary = healthSummary,
                    insights = insights
                )
            } catch (e: Exception) {
                _uiState.value = WearablesUiState.Error(
                    e.message ?: "Failed to load wearables data"
                )
            }
        }
    }
    
    fun syncWearableData(deviceId: String, dataPoints: List<DataPointDto>) {
        viewModelScope.launch {
            try {
                // Get patient UID from stored preferences
                val patientUid = getStoredPatientUid()
                
                val result = repository.syncWearableData(patientUid, deviceId, dataPoints)
                
                if (result.isSuccess) {
                    // Reload data after successful sync
                    loadWearablesData()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
```

---

## 7. Token Storage with DataStore

### `data/local/TokenManager.kt`
```kotlin
package com.example.cloudcareapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TokenManager(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("auth")
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val PATIENT_UID_KEY = stringPreferencesKey("patient_uid")
        private val USER_TYPE_KEY = stringPreferencesKey("user_type")
    }
    
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
    }
    
    suspend fun savePatientUid(uid: String) {
        context.dataStore.edit { preferences ->
            preferences[PATIENT_UID_KEY] = uid
        }
    }
    
    suspend fun saveUserType(userType: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_TYPE_KEY] = userType
        }
    }
    
    fun getAuthToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN_KEY]
        }
    }
    
    fun getPatientUid(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[PATIENT_UID_KEY]
        }
    }
    
    suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
```

---

## 8. Update Login Screen

### `ui/screens/auth/PatientLoginScreen.kt`
```kotlin
@Composable
fun PatientLoginScreen(
    onLoginSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val loginState by viewModel.loginState.collectAsState()
    
    // ... existing UI code ...
    
    Button(
        onClick = {
            viewModel.login(
                email = email,
                password = password,
                userType = "patient",
                onSuccess = {
                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                },
                onError = { error ->
                    Toast.makeText(context, "Login failed: $error", Toast.LENGTH_SHORT).show()
                }
            )
        },
        enabled = !loginState.isLoading
    ) {
        if (loginState.isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        } else {
            Text("Login")
        }
    }
}
```

### `ui/screens/auth/LoginViewModel.kt`
```kotlin
class LoginViewModel(
    private val repository: CloudCareRepository = CloudCareRepository(),
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    
    fun login(
        email: String,
        password: String,
        userType: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _loginState.value = LoginState(isLoading = true)
            
            val result = repository.login(email, password, userType)
            
            result.fold(
                onSuccess = { response ->
                    // Save token
                    tokenManager.saveAuthToken(response.accessToken)
                    response.patientUid?.let { tokenManager.savePatientUid(it) }
                    tokenManager.saveUserType(response.userType)
                    
                    // Set token in Retrofit client
                    RetrofitClient.setAuthToken(response.accessToken)
                    
                    _loginState.value = LoginState(isLoading = false, isSuccess = true)
                    onSuccess()
                },
                onFailure = { error ->
                    _loginState.value = LoginState(
                        isLoading = false,
                        error = error.message ?: "Login failed"
                    )
                    onError(error.message ?: "Login failed")
                }
            )
        }
    }
}

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
```

---

## 9. Health Data Sync Service

Create background service for periodic sync:

### `services/HealthSyncWorker.kt`
```kotlin
package com.example.cloudcareapp.services

import android.content.Context
import androidx.work.*
import com.example.cloudcareapp.data.repository.CloudCareRepository
import java.util.concurrent.TimeUnit

class HealthSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val repository = CloudCareRepository()
    
    override suspend fun doWork(): Result {
        return try {
            // Get health data from Google Fit / HealthConnect
            val healthData = collectHealthData()
            
            // Sync to backend
            repository.syncWearableData(
                patientUid = getPatientUid(),
                deviceId = getDeviceId(),
                dataPoints = healthData
            )
            
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    companion object {
        fun schedulePeriodicSync(context: Context) {
            val syncRequest = PeriodicWorkRequestBuilder<HealthSyncWorker>(
                15, TimeUnit.MINUTES // Sync every 15 minutes
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "health_sync",
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest
                )
        }
    }
}
```

---

## 10. Configuration

### Update `local.properties`
```properties
# Backend configuration
backend.url=https://api.cloudcare.com/
# For local testing: http://10.0.2.2:5000/ (emulator)
# For local testing: http://192.168.1.100:5000/ (physical device)

backend.debug=true
```

### Add Internet Permission in `AndroidManifest.xml`
```xml
<manifest ...>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <!-- For health data access -->
    <uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
    
    <application
        android:usesCleartextTraffic="true"  <!-- Only for development -->
        ...
    >
    </application>
</manifest>
```

---

## Testing Backend Connection

### Test with Local Backend

1. **Start Flask Server**:
   ```bash
   cd backend
   python app.py
   ```

2. **Find Your IP** (for physical device):
   ```bash
   # macOS
   ipconfig getifaddr en0
   
   # Linux
   hostname -I
   ```

3. **Update Base URL** in `RetrofitClient.kt`:
   ```kotlin
   private const val BASE_URL = "http://YOUR_IP:5000/"
   ```

4. **Test Login**:
   - Run Android app
   - Try logging in with credentials
   - Check Logcat for network requests

### Debug Network Issues

Add logging interceptor to see requests/responses:
```kotlin
val loggingInterceptor = HttpLoggingInterceptor { message ->
    Log.d("API", message)
}.apply {
    level = HttpLoggingInterceptor.Level.BODY
}
```

---

## Summary

You now have:
- ✅ Retrofit setup for API calls
- ✅ Request/Response models matching backend
- ✅ Real repository replacing mock data
- ✅ Token management with DataStore
- ✅ Background sync for health data
- ✅ Proper error handling

**Next Steps:**
1. Start Flask backend server
2. Update `BASE_URL` in RetrofitClient
3. Test login flow
4. Verify data syncing
5. Test wearable data upload

---

**End of Integration Guide**
