package com.example.cloudcareapp.data.repository

import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.AdmitPatientRequest
import com.example.cloudcareapp.data.model.DischargePatientRequest
import com.example.cloudcareapp.data.model.ResourceUpdate
import com.example.cloudcareapp.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Hospital-related data operations
 */
class HospitalRepository {
    
    private val apiService = RetrofitClient.apiService
    
    /**
     * Get hospital profile by ID
     */
    suspend fun getHospitalProfile(hospitalId: String, forceRefresh: Boolean = false) = withContext(Dispatchers.IO) {
        if (!forceRefresh && AppDataCache.getHospitalProfile() != null) {
            return@withContext AppDataCache.getHospitalProfile()!!
        }
        val profile = apiService.getHospitalProfile(hospitalId)
        AppDataCache.setHospitalProfile(profile)
        profile
    }
    
    /**
     * Get hospital dashboard stats
     */
    suspend fun getDashboardStats(hospitalId: String, forceRefresh: Boolean = false) = withContext(Dispatchers.IO) {
        if (!forceRefresh && AppDataCache.getHospitalDashboardStats() != null) {
            return@withContext AppDataCache.getHospitalDashboardStats()!!
        }
        val stats = apiService.getHospitalDashboardStats(hospitalId)
        AppDataCache.setHospitalDashboardStats(stats)
        stats
    }

    /**
     * Get list of doctors
     */
    suspend fun getDoctors(hospitalId: String, forceRefresh: Boolean = false) = withContext(Dispatchers.IO) {
        if (!forceRefresh && AppDataCache.getHospitalDoctors().isNotEmpty()) {
            return@withContext AppDataCache.getHospitalDoctors()
        }
        val doctors = apiService.getHospitalDoctors(hospitalId)
        AppDataCache.setHospitalDoctors(doctors)
        doctors
    }

    /**
     * Get list of patients
     */
    suspend fun getPatients(hospitalId: String, forceRefresh: Boolean = false, statusFilter: String? = null) = withContext(Dispatchers.IO) {
        // If filtering, we might skip cache or have separate cache keys. 
        // For simplicity, if filter is present, skip cache or just fetch fresh.
        if (statusFilter == null && !forceRefresh && AppDataCache.getHospitalPatients().isNotEmpty()) {
            return@withContext AppDataCache.getHospitalPatients()
        }
        val patients = apiService.getHospitalPatients(hospitalId, statusFilter)
        if (statusFilter == null) {
            AppDataCache.setHospitalPatients(patients)
        }
        patients
    }

    /**
     * Discharge a patient
     */
    suspend fun dischargePatient(hospitalId: String, patientId: String, note: String, documentUrl: String? = null) = withContext(Dispatchers.IO) {
        apiService.dischargePatient(hospitalId, patientId, DischargePatientRequest(note, documentUrl))
    }

    /**
     * Update hospital resources
     */
    suspend fun updateResources(hospitalId: String, resources: ResourceUpdate) = withContext(Dispatchers.IO) {
        apiService.updateHospitalResources(hospitalId, resources)
    }

    /**
     * Admit a patient
     */
    suspend fun admitPatient(hospitalId: String, aadharNumber: String, reason: String) = withContext(Dispatchers.IO) {
        apiService.admitPatient(hospitalId, AdmitPatientRequest(aadharNumber, reason))
    }

    /**
     * Update hospital profile
     */
    suspend fun updateHospitalProfile(hospitalId: String, request: Any): Nothing = withContext(Dispatchers.IO) {
        // TODO: Implement when update endpoint is needed
        throw NotImplementedError("Update hospital profile not yet implemented")
    }
}
