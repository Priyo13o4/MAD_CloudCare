# Patient Authentication Setup - Copilot Agent Prompt

## OBJECTIVE
Implement complete patient user authentication for the CloudCare Android app with persistent login using secure token storage. This includes backend registration/login endpoints and Android app authentication flow with SharedPreferences-based session management.

## CONTEXT & CONSTRAINTS

### Current State
- **Backend**: FastAPI server with PostgreSQL (Prisma ORM)
- **Database Schema**: Uses snake_case field names (User, Patient tables exist)
- **Android App**: Kotlin + Jetpack Compose, currently NO authentication
- **API Base URL**: `https://cloudcare.pipfactor.com/api/v1/`
- **Current Patient ID**: Hardcoded as `"test_patient_001"` in ViewModels
- **Data Format**: All API responses now use snake_case (recently standardized)

### Requirements
1. **PATIENT ROLE ONLY** - No doctor/hospital registration needed
2. **Persistent Login** - User stays logged in until explicit logout
3. **Token-Based Auth** - JWT tokens stored securely in Android app cache
4. **Required Fields**: email, password, name, age, gender, blood_type, contact, address
5. **Optional Fields**: family_contact, insurance_provider, insurance_id, occupation
6. **Aadhar UID**: Auto-generate UUID for testing (not real Aadhar validation)

## FILES TO CHECK/MODIFY

### Backend Files (FastAPI)
```
backend/
├── app/
│   ├── api/v1/
│   │   ├── auth.py                    # ⚠️ CHECK - May need updates for patient registration
│   │   └── patient.py                 # ⚠️ CHECK - May have conflicting endpoints
│   ├── services/
│   │   └── auth_service.py            # ⚠️ CHECK - JWT token generation/validation
│   └── models/
│       └── auth.py                    # ⚠️ CHECK - Pydantic models for auth
├── prisma/
│   └── schema.prisma                  # ✅ READY - Users & Patients tables exist (snake_case)
└── main.py                            # ⚠️ CHECK - Ensure auth routes are registered
```

### Android Files (Kotlin)
```
CloudCare Android App/app/src/main/java/com/example/cloudcareapp/
├── data/
│   ├── model/
│   │   ├── Auth.kt                    # 🆕 CREATE - Login/Register request/response models
│   │   └── Patient.kt                 # ⚠️ UPDATE - Remove @SerializedName (now snake_case)
│   ├── remote/
│   │   └── CloudCareApiService.kt     # 🆕 ADD - Login/register endpoints
│   └── repository/
│       └── AuthRepository.kt          # 🆕 CREATE - Auth API calls
├── ui/
│   ├── screens/
│   │   ├── LoginScreen.kt             # 🆕 CREATE - Email/password login UI
│   │   ├── RegisterScreen.kt          # 🆕 CREATE - Patient registration form
│   │   └── SplashScreen.kt            # 🆕 CREATE - Check auth status on app launch
│   └── viewmodel/
│       └── AuthViewModel.kt           # 🆕 CREATE - Auth state management
├── utils/
│   ├── AuthManager.kt                 # 🆕 CREATE - Token storage/retrieval (SharedPreferences)
│   └── SessionManager.kt              # 🆕 CREATE - Check login status, auto-logout
└── MainActivity.kt                    # ⚠️ UPDATE - Add auth navigation, check login status
```

## IMPLEMENTATION TASKS

### Phase 1: Backend Authentication API

#### Task 1.1: Review & Update Auth Endpoints
**Check**: `backend/app/api/v1/auth.py`
- Verify `/register` endpoint exists for PATIENT role
- Ensure it uses snake_case field names (patient_id, created_at, etc.)
- Required response: `{ "access_token": "jwt...", "token_type": "bearer", "user": {...}, "patient": {...} }`

**Update if needed**:
```python
@router.post("/register/patient")
async def register_patient(
    email: str,
    password: str,
    name: str,
    age: int,
    gender: str,
    blood_type: str,
    contact: str,
    address: str,
    family_contact: Optional[str] = None,
    insurance_provider: Optional[str] = None,
    insurance_id: Optional[str] = None,
    occupation: Optional[str] = None
):
    # 1. Validate email not already registered
    # 2. Hash password (use bcrypt/passlib)
    # 3. Create User with role=PATIENT
    # 4. Generate aadhar_uid (UUID for testing)
    # 5. Create Patient record linked to User
    # 6. Generate JWT token (use auth_service.py)
    # 7. Return token + patient data
```

**Login Endpoint**:
```python
@router.post("/login")
async def login(email: str, password: str):
    # 1. Find user by email
    # 2. Verify password hash
    # 3. Check role == PATIENT
    # 4. Generate JWT token
    # 5. Return token + patient data
```

#### Task 1.2: Verify JWT Token Service
**Check**: `backend/app/services/auth_service.py`
- Ensure `create_access_token(data: dict)` exists
- Ensure `decode_token(token: str)` exists
- Token payload should include: `{ "sub": user_id, "role": "PATIENT", "exp": timestamp }`

#### Task 1.3: Update Existing Patient-ID-Dependent Endpoints
**Files to check**: `backend/app/api/v1/wearables.py`, `patient.py`
- Replace `get_current_patient_id()` dependency to decode from JWT token
- Remove any hardcoded `test_patient_001` references
- Ensure all Prisma queries use snake_case field names

**Example**:
```python
async def get_current_patient_id(credentials: HTTPAuthorizationCredentials = Depends(security)) -> str:
    token = credentials.credentials
    payload = AuthService.decode_token(token)
    
    if not payload or payload.get("role") != "PATIENT":
        raise HTTPException(status_code=403, detail="Patient access required")
    
    prisma = get_prisma()
    user = await prisma.user.find_unique(
        where={"id": payload["sub"]},
        include={"patient": True}
    )
    
    if not user or not user.patient:
        raise HTTPException(status_code=404, detail="Patient profile not found")
    
    return user.patient.id  # Return patient UUID
```

### Phase 2: Android Authentication Implementation

#### Task 2.1: Create Auth Data Models
**Create**: `Auth.kt`
```kotlin
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val age: Int,
    val gender: String,
    val blood_type: String,
    val contact: String,
    val address: String,
    val family_contact: String? = null,
    val insurance_provider: String? = null,
    val insurance_id: String? = null,
    val occupation: String? = null
)

data class AuthResponse(
    val access_token: String,
    val token_type: String,
    val user: UserData,
    val patient: PatientData
)

data class UserData(
    val id: String,
    val email: String,
    val role: String
)

data class PatientData(
    val id: String,
    val user_id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val blood_type: String,
    val contact: String,
    val email: String,
    val address: String,
    val family_contact: String?,
    val created_at: String
)
```

#### Task 2.2: Update Patient.kt Models
**Remove all `@SerializedName` annotations** - Backend now uses snake_case natively
```kotlin
// BEFORE (incorrect):
@SerializedName("patient_id")
val patientId: String

// AFTER (correct):
val patient_id: String
```

#### Task 2.3: Add Auth API Endpoints
**Update**: `CloudCareApiService.kt`
```kotlin
@POST("auth/register/patient")
suspend fun registerPatient(@Body request: RegisterRequest): AuthResponse

@POST("auth/login")
suspend fun login(@Body request: LoginRequest): AuthResponse

@POST("auth/logout")
suspend fun logout(@Header("Authorization") token: String): Response<Unit>
```

#### Task 2.4: Create AuthManager (Token Storage)
**Create**: `AuthManager.kt`
```kotlin
object AuthManager {
    private const val PREFS_NAME = "cloudcare_auth"
    private const val KEY_TOKEN = "access_token"
    private const val KEY_PATIENT_ID = "patient_id"
    private const val KEY_USER_EMAIL = "user_email"
    
    fun saveAuthToken(context: Context, token: String, patientId: String, email: String)
    fun getAuthToken(context: Context): String?
    fun getPatientId(context: Context): String?
    fun isLoggedIn(context: Context): Boolean
    fun clearAuth(context: Context)
}
```

#### Task 2.5: Create AuthRepository
**Create**: `AuthRepository.kt`
```kotlin
class AuthRepository(
    private val apiService: CloudCareApiService = RetrofitClient.apiService
) {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun logout(token: String): Result<Unit>
}
```

#### Task 2.6: Create AuthViewModel
**Create**: `AuthViewModel.kt`
```kotlin
class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    
    fun login(email: String, password: String)
    fun register(request: RegisterRequest)
    fun logout()
    fun checkAuthStatus(context: Context): Boolean
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val response: AuthResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}
```

#### Task 2.7: Create Login Screen
**Create**: `LoginScreen.kt`
- Email TextField (email validation)
- Password TextField (visibility toggle)
- Login Button (shows loading state)
- "Don't have an account? Register" link
- Error message display

#### Task 2.8: Create Register Screen
**Create**: `RegisterScreen.kt`
- All required fields (email, password, confirm password, name, age, gender, blood_type, contact, address)
- Optional fields (family_contact, insurance details, occupation)
- Gender dropdown (Male/Female/Other)
- Blood type dropdown (A+, A-, B+, B-, AB+, AB-, O+, O-)
- Form validation (email format, password strength, required fields)
- "Already have an account? Login" link

#### Task 2.9: Create Splash Screen
**Create**: `SplashScreen.kt`
```kotlin
@Composable
fun SplashScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        delay(2000) // Show logo for 2 seconds
        val isLoggedIn = AuthManager.isLoggedIn(context)
        onNavigate(if (isLoggedIn) "home" else "login")
    }
}
```

#### Task 2.10: Update Navigation & MainActivity
**Update**: `MainActivity.kt`
```kotlin
// Add auth navigation
NavHost(startDestination = "splash") {
    composable("splash") { SplashScreen(navController::navigate) }
    composable("login") { LoginScreen(navController) }
    composable("register") { RegisterScreen(navController) }
    composable("home") { /* Existing home screen */ }
    // ... other screens
}
```

#### Task 2.11: Update All ViewModels
**Files**: `WearablesViewModel.kt`, `HealthViewModel.kt`, etc.
- Replace hardcoded `patientId = "test_patient_001"` 
- Get patient ID from AuthManager: `AuthManager.getPatientId(context)`
- Add token to API calls: `@Header("Authorization") "Bearer $token"`

#### Task 2.12: Add Interceptor for Auth Token
**Update**: `RetrofitClient.kt`
```kotlin
private val authInterceptor = Interceptor { chain ->
    val token = AuthManager.getAuthToken(context)
    val request = if (token != null) {
        chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
    } else {
        chain.request()
    }
    chain.proceed(request)
}

val retrofit = Retrofit.Builder()
    .client(OkHttpClient.Builder().addInterceptor(authInterceptor).build())
    // ...
```

## TESTING CHECKLIST

### Backend Tests
- [ ] POST `/auth/register/patient` creates User + Patient with snake_case fields
- [ ] POST `/auth/login` returns valid JWT token
- [ ] JWT token payload contains `{ "sub": user_id, "role": "PATIENT" }`
- [ ] Protected endpoints reject requests without token (401)
- [ ] Protected endpoints reject non-PATIENT tokens (403)
- [ ] GET `/wearables/devices/paired?android_user_id=UUID` works with real patient ID

### Android Tests
- [ ] Login screen validates email format
- [ ] Register screen validates required fields
- [ ] Successful login saves token to SharedPreferences
- [ ] App remembers login after restart
- [ ] Logout clears token and navigates to login
- [ ] WearablesScreen loads data using authenticated patient ID
- [ ] Splash screen correctly routes to login vs home

## DATABASE RESET REQUIRED

**⚠️ IMPORTANT**: The schema has been updated to snake_case. Old data must be wiped.

**Run this command before starting**:
```bash
cd backend
chmod +x reset_db.sh
./reset_db.sh
```

This will:
1. Regenerate Prisma client with snake_case fields
2. Delete all existing data
3. Recreate tables with new schema
4. All old patient IDs (like `test_patient_001`) will be gone

## EXPECTED FINAL FLOW

```
1. User opens app → Splash screen
2. Check AuthManager.isLoggedIn()
   ├─ Yes → Navigate to Home (WearablesScreen)
   └─ No  → Navigate to Login
3. User enters email/password → Click Login
4. API call to /auth/login
5. Backend validates credentials, returns JWT token
6. Android saves token + patient_id to SharedPreferences
7. Navigate to Home
8. WearablesViewModel loads data using patient_id from AuthManager
9. All API calls include "Authorization: Bearer {token}" header
10. User clicks Logout → Clear SharedPreferences → Navigate to Login
```

## COMMON ISSUES TO AVOID

1. **Case Mismatch**: Backend now uses snake_case natively - NO @SerializedName needed on Android
2. **Token Expiration**: JWT tokens should have reasonable expiry (e.g., 7 days)
3. **Hardcoded Patient ID**: Remove ALL instances of `"test_patient_001"`
4. **Missing Authorization Header**: Update RetrofitClient to auto-inject token
5. **Password Storage**: NEVER store plain passwords - only JWT token
6. **Navigation Logic**: Splash screen must check auth status BEFORE showing login

## SUCCESS CRITERIA

✅ New user can register with email/password
✅ Registered user can login and see their wearable devices
✅ Login persists across app restarts
✅ User can logout and must login again
✅ All API calls use real patient UUID from JWT
✅ Backend rejects unauthenticated requests
✅ No `@SerializedName` annotations needed (snake_case everywhere)

## ADDITIONAL NOTES

- **Password Requirements**: Minimum 8 characters, at least 1 uppercase, 1 number
- **Email Validation**: Use Android's `Patterns.EMAIL_ADDRESS.matcher(email).matches()`
- **Error Messages**: User-friendly messages for "Email already exists", "Invalid credentials", etc.
- **Loading States**: Show spinners during API calls
- **Form State**: Preserve form data on orientation change using `rememberSaveable`

---

**Start with Phase 1 (Backend) first, then Phase 2 (Android). Test each phase before moving to the next.**
