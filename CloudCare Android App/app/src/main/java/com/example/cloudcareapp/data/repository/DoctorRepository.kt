package com.example.cloudcareapp.data.repository

import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Doctor-related data operations
 */
class DoctorRepository {
    
    private val apiService = RetrofitClient.apiService
    
    /**
     * Get doctor profile by ID
     */
    suspend fun getDoctorProfile(doctorId: String, forceRefresh: Boolean = false) = withContext(Dispatchers.IO) {
        if (!forceRefresh && AppDataCache.getDoctorProfile() != null) {
            return@withContext AppDataCache.getDoctorProfile()!!
        }
        try {
            val profile = apiService.getDoctorProfile(doctorId)
            AppDataCache.setDoctorProfile(profile)
            profile
        } catch (e: Exception) {
            throw Exception("Failed to fetch doctor profile: ${e.message}")
        }
    }
    
    /**
     * Update doctor profile
     */
    suspend fun updateDoctorProfile(doctorId: String, request: Any): Nothing = withContext(Dispatchers.IO) {
        // TODO: Implement when update endpoint is needed
        throw NotImplementedError("Update doctor profile not yet implemented")
    }
}
