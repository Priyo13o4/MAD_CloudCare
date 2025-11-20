package com.example.cloudcareapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.*
import com.example.cloudcareapp.data.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing authentication state and operations
 * 
 * Provides:
 * - User registration for all roles
 * - Login/logout functionality
 * - Session state management
 * - Loading and error state handling
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = AuthRepository(application.applicationContext)
    
    // ==================== LiveData State ====================
    
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _userSession = MutableLiveData<UserSession?>()
    val userSession: LiveData<UserSession?> = _userSession
    
    init {
        // Check if user is already logged in
        checkLoginStatus()
    }
    
    // ==================== Authentication States ====================
    
    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val session: UserSession) : AuthState()
        data class Error(val message: String) : AuthState()
        object LoggedOut : AuthState()
    }
    
    // ==================== Registration Functions ====================
    
    fun registerPatient(request: RegisterPatientRequest) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.registerPatient(request)
            handleAuthResult(result)
        }
    }
    
    fun registerDoctor(request: RegisterDoctorRequest) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.registerDoctor(request)
            handleAuthResult(result)
        }
    }
    
    fun registerHospital(request: RegisterHospitalRequest) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.registerHospital(request)
            handleAuthResult(result)
        }
    }
    
    // ==================== Login/Logout ====================
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.login(email, password)
            handleAuthResult(result)
        }
    }
    
    fun loginWithAadhar(aadhar: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.loginWithAadhar(aadhar, password)
            handleAuthResult(result)
        }
    }
    
    fun logout() {
        repository.logout()
        _authState.value = AuthState.LoggedOut
        _userSession.value = null
        _errorMessage.value = null
    }
    
    // ==================== Session Management ====================
    
    fun checkLoginStatus() {
        val session = repository.getUserSession()
        if (session != null) {
            _userSession.value = session
            _authState.value = AuthState.Success(session)
            // ✅ FIX: Populate AppDataCache when restoring session
            populateAppDataCache(session.user)
        } else {
            _authState.value = AuthState.LoggedOut
        }
    }
    
    fun refreshToken() {
        viewModelScope.launch {
            val result = repository.refreshToken()
            handleAuthResult(result)
        }
    }
    
    fun isLoggedIn(): Boolean {
        return repository.isLoggedIn()
    }
    
    fun getAuthorizationHeader(): String? {
        return repository.getAuthorizationHeader()
    }
    
    fun getCurrentUser(): AuthUserResponse? {
        return _userSession.value?.user
    }
    
    // ==================== State Utilities ====================
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun resetAuthState() {
        _authState.value = AuthState.Idle
        _errorMessage.value = null
    }
    
    // ==================== Helper Functions ====================
    
    private fun handleAuthResult(result: Result<TokenResponse>) {
        _isLoading.value = false
        
        result.onSuccess { tokenResponse ->
            val session = UserSession(
                accessToken = tokenResponse.accessToken,
                refreshToken = tokenResponse.refreshToken,
                user = tokenResponse.user
            )
            _userSession.value = session
            _authState.value = AuthState.Success(session)
            _errorMessage.value = null
            // ✅ FIX: Populate AppDataCache after successful login
            populateAppDataCache(tokenResponse.user)
        }.onFailure { error ->
            val message = error.message ?: "Authentication failed"
            _errorMessage.value = message
            _authState.value = AuthState.Error(message)
        }
    }
    
    /**
     * Populate AppDataCache with user IDs from auth response
     * This ensures all screens can access patient/doctor/hospital IDs
     */
    private fun populateAppDataCache(user: AuthUserResponse) {
        user.patientId?.let { AppDataCache.setPatientId(it) }
        user.doctorId?.let { AppDataCache.setDoctorId(it) }
        user.hospitalId?.let { AppDataCache.setHospitalId(it) }
        user.name?.let { AppDataCache.setUserName(it) }
    }
}
