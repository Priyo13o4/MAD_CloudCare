package com.example.cloudcareapp.data.repository

import com.example.cloudcareapp.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Doctor-related data operations
 * Always fetches live data from API - no caching
 */
class DoctorRepository {
    
    private val apiService = RetrofitClient.apiService
    
    /**
     * Get doctor profile by ID
     * Always fetches fresh data from API
     */
    suspend fun getDoctorProfile(doctorId: String) = withContext(Dispatchers.IO) {
        try {
            apiService.getDoctorProfile(doctorId)
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
