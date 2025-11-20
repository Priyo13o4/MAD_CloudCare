package com.example.cloudcareapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.*
import com.example.cloudcareapp.data.remote.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Repository for managing authentication operations
 * 
 * Handles:
 * - User registration (patient, doctor, hospital)
 * - Login/logout
 * - Token management and refresh
 * - Session persistence in SharedPreferences
 */
class AuthRepository(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("cloudcare_auth", Context.MODE_PRIVATE)
    
    private val gson = Gson()
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_DATA = "user_data"
    }
    
    // ==================== Registration ====================
    
    suspend fun registerPatient(request: RegisterPatientRequest): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.authApiService.registerPatient(request)
                handleAuthResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun registerDoctor(request: RegisterDoctorRequest): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.authApiService.registerDoctor(request)
                handleAuthResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun registerHospital(request: RegisterHospitalRequest): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.authApiService.registerHospital(request)
                handleAuthResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // ==================== Login/Logout ====================
    
    suspend fun login(email: String, password: String): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email = email, aadhar = null, password = password)
                val response = RetrofitClient.authApiService.login(request)
                handleAuthResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun loginWithAadhar(aadhar: String, password: String): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email = null, aadhar = aadhar, password = password)
                val response = RetrofitClient.authApiService.login(request)
                handleAuthResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    fun logout() {
        sharedPreferences.edit().clear().apply()
    }
    
    // ==================== Token Management ====================
    
    suspend fun refreshToken(): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val currentRefreshToken = getRefreshToken()
                    ?: return@withContext Result.failure(Exception("No refresh token available"))
                
                val request = RefreshTokenRequest(currentRefreshToken)
                val response = RetrofitClient.authApiService.refreshToken(request)
                handleAuthResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // ==================== Session Management ====================
    
    fun saveSession(tokenResponse: TokenResponse) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, tokenResponse.accessToken)
            putString(KEY_REFRESH_TOKEN, tokenResponse.refreshToken)
            putString(KEY_USER_DATA, gson.toJson(tokenResponse.user))
            apply()
        }
        
        // ✅ FIX: Populate AppDataCache with user IDs
        val user = tokenResponse.user
        user.patientId?.let { AppDataCache.setPatientId(it) }
        user.doctorId?.let { AppDataCache.setDoctorId(it) }
        user.hospitalId?.let { AppDataCache.setHospitalId(it) }
        // Note: User name will be fetched from profile endpoint separately
    }
    
    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }
    
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }
    
    fun getUserSession(): UserSession? {
        val accessToken = getAccessToken() ?: return null
        val refreshToken = getRefreshToken() ?: return null
        val userJson = sharedPreferences.getString(KEY_USER_DATA, null) ?: return null
        
        return try {
            val user = gson.fromJson(userJson, AuthUserResponse::class.java)
            UserSession(accessToken, refreshToken, user)
        } catch (e: Exception) {
            null
        }
    }
    
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null && getUserSession() != null
    }
    
    fun getAuthorizationHeader(): String? {
        val token = getAccessToken() ?: return null
        return "Bearer $token"
    }
    
    // ==================== Helper Functions ====================
    
    private fun handleAuthResponse(response: Response<TokenResponse>): Result<TokenResponse> {
        return if (response.isSuccessful) {
            val tokenResponse = response.body()
            if (tokenResponse != null) {
                saveSession(tokenResponse)
                Result.success(tokenResponse)
            } else {
                Result.failure(Exception("Empty response body"))
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = try {
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.detail ?: errorResponse.message ?: "Authentication failed"
            } catch (e: Exception) {
                "Authentication failed: ${response.code()}"
            }
            Result.failure(Exception(errorMessage))
        }
    }
}
