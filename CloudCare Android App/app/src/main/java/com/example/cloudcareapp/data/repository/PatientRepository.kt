package com.example.cloudcareapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.cloudcareapp.data.model.PatientProfileData
import com.example.cloudcareapp.data.model.PatientProfileResponse
import com.example.cloudcareapp.data.remote.CloudCareApiService
import com.example.cloudcareapp.data.remote.RetrofitClient
import com.google.gson.Gson

/**
 * Repository for patient profile data with caching
 */
class PatientRepository(context: Context) {
    
    private val apiService: CloudCareApiService = RetrofitClient.apiService
    private val prefs: SharedPreferences = context.getSharedPreferences("patient_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val TAG = "PatientRepository"
        private const val KEY_PATIENT_PROFILE = "patient_profile_cache"
        private const val KEY_CACHE_TIMESTAMP = "patient_profile_cache_timestamp"
        private const val CACHE_DURATION_MS = 5 * 60 * 1000 // 5 minutes
    }
    
    /**
     * Get patient profile with caching
     * - First checks cache (valid for 5 minutes)
     * - Falls back to API if cache is stale or missing
     */
    suspend fun getPatientProfile(patientId: String, forceRefresh: Boolean = false): Result<PatientProfileData> {
        return try {
            // Check cache first if not forcing refresh
            if (!forceRefresh) {
                val cachedProfile = getCachedProfile()
                if (cachedProfile != null && isCacheValid()) {
                    Log.d(TAG, "Returning cached patient profile")
                    return Result.success(cachedProfile)
                }
            }
            
            // Fetch from API
            Log.d(TAG, "Fetching patient profile from API for patientId: $patientId")
            val response = apiService.getPatientProfile(patientId)
            
            if (response.success && response.patient != null) {
                // Cache the result
                cacheProfile(response.patient)
                Result.success(response.patient)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch patient profile"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching patient profile", e)
            
            // Try to return cached data even if stale
            val cachedProfile = getCachedProfile()
            if (cachedProfile != null) {
                Log.d(TAG, "Returning stale cached data due to API error")
                Result.success(cachedProfile)
            } else {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Cache patient profile data
     */
    fun cacheProfile(profile: PatientProfileData) {
        try {
            val json = gson.toJson(profile)
            prefs.edit()
                .putString(KEY_PATIENT_PROFILE, json)
                .putLong(KEY_CACHE_TIMESTAMP, System.currentTimeMillis())
                .apply()
            Log.d(TAG, "Patient profile cached successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error caching patient profile", e)
        }
    }
    
    /**
     * Get cached patient profile
     */
    private fun getCachedProfile(): PatientProfileData? {
        return try {
            val json = prefs.getString(KEY_PATIENT_PROFILE, null)
            if (json != null) {
                gson.fromJson(json, PatientProfileData::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading cached profile", e)
            null
        }
    }
    
    /**
     * Check if cache is still valid
     */
    private fun isCacheValid(): Boolean {
        val cacheTimestamp = prefs.getLong(KEY_CACHE_TIMESTAMP, 0)
        val now = System.currentTimeMillis()
        val isValid = (now - cacheTimestamp) < CACHE_DURATION_MS
        Log.d(TAG, "Cache valid: $isValid (age: ${(now - cacheTimestamp) / 1000}s)")
        return isValid
    }
    
    /**
     * Clear cached profile data
     */
    fun clearCache() {
        prefs.edit()
            .remove(KEY_PATIENT_PROFILE)
            .remove(KEY_CACHE_TIMESTAMP)
            .apply()
        Log.d(TAG, "Patient profile cache cleared")
    }
    
    /**
     * Check if profile is cached
     */
    fun hasCache(): Boolean {
        return getCachedProfile() != null
    }
}
