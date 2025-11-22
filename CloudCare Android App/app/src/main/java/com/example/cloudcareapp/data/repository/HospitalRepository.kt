package com.example.cloudcareapp.data.repository

import com.example.cloudcareapp.data.model.AdmitPatientRequest
import com.example.cloudcareapp.data.model.DischargePatientRequest
import com.example.cloudcareapp.data.model.ResourceUpdate
import com.example.cloudcareapp.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for Hospital-related data operations
 * Always fetches live data from API - no caching
 */
class HospitalRepository {
    
    private val apiService = RetrofitClient.apiService
    
    /**
     * Get hospital profile by ID
     * Always fetches fresh data from API
     */
    suspend fun getHospitalProfile(hospitalId: String) = withContext(Dispatchers.IO) {
        apiService.getHospitalProfile(hospitalId)
    }
    
    /**
     * Get hospital dashboard stats
     * Always fetches fresh data from API
     */
    suspend fun getDashboardStats(hospitalId: String) = withContext(Dispatchers.IO) {
        apiService.getHospitalDashboardStats(hospitalId)
    }

    /**
     * Get list of doctors
     * Always fetches fresh data from API
     */
    suspend fun getDoctors(hospitalId: String) = withContext(Dispatchers.IO) {
        apiService.getHospitalDoctors(hospitalId)
    }

    /**
     * Get list of patients
     * Always fetches fresh data from API
     */
    suspend fun getPatients(hospitalId: String, statusFilter: String? = null) = withContext(Dispatchers.IO) {
        apiService.getHospitalPatients(hospitalId, statusFilter)
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
     * Admit a patient by Aadhar or patient ID
     */
    suspend fun admitPatient(
        hospitalId: String, 
        aadharNumber: String? = null, 
        patientId: String? = null,
        reason: String
    ) = withContext(Dispatchers.IO) {
        apiService.admitPatient(hospitalId, AdmitPatientRequest(aadharNumber, patientId, reason))
    }

    /**
     * Update hospital profile
     */
    suspend fun updateHospitalProfile(hospitalId: String, request: Any): Nothing = withContext(Dispatchers.IO) {
        // TODO: Implement when update endpoint is needed
        throw NotImplementedError("Update hospital profile not yet implemented")
    }
}
