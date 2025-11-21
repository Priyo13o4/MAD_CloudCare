package com.example.cloudcareapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.DoctorPatientResponse
import com.example.cloudcareapp.data.model.DoctorProfileResponse
import com.example.cloudcareapp.data.model.HospitalAssociation
import com.example.cloudcareapp.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class DoctorProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = com.example.cloudcareapp.data.repository.DoctorRepository()

    private val _doctorProfile = MutableLiveData<DoctorProfileResponse?>()
    val doctorProfile: LiveData<DoctorProfileResponse?> = _doctorProfile

    private val _patients = MutableLiveData<List<DoctorPatientResponse>>(emptyList())
    val patients: LiveData<List<DoctorPatientResponse>> = _patients
    
    private val _doctorHospitals = MutableLiveData<List<HospitalAssociation>>(emptyList())
    val doctorHospitals: LiveData<List<HospitalAssociation>> = _doctorHospitals

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun loadDoctorProfile(doctorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val profile = repository.getDoctorProfile(doctorId)
                _doctorProfile.value = profile
            } catch (e: Exception) {
                _error.value = "Failed to load profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadDoctorPatients(doctorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val patientsList = RetrofitClient.apiService.getDoctorPatients(doctorId)
                _patients.value = patientsList
            } catch (e: Exception) {
                _error.value = "Failed to load patients: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadDoctorHospitals(doctorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.apiService.getDoctorHospitals(doctorId)
                if (response.isSuccessful) {
                    _doctorHospitals.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Failed to load hospitals: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load hospitals: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    suspend fun updateDoctorHospitals(doctorId: String, hospitalIds: List<String>): Boolean {
        return try {
            android.util.Log.d("DoctorProfileVM", "Updating hospitals for doctor $doctorId with IDs: $hospitalIds")
            val requestBody = com.example.cloudcareapp.data.model.UpdateDoctorHospitalsRequest(
                hospitalIds = hospitalIds
            )
            val response = RetrofitClient.apiService.updateDoctorHospitals(doctorId, requestBody)
            android.util.Log.d("DoctorProfileVM", "Hospital update response: ${response.code()} - ${response.message()}")
            
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("DoctorProfileVM", "Hospital update failed: $errorBody")
                _error.value = "Failed to update hospitals: ${response.message()}"
            }
            response.isSuccessful
        } catch (e: Exception) {
            android.util.Log.e("DoctorProfileVM", "Hospital update exception", e)
            _error.value = "Failed to update hospitals: ${e.message}"
            false
        }
    }
    
    fun refresh(doctorId: String) {
        loadDoctorProfile(doctorId)
        loadDoctorPatients(doctorId)
        loadDoctorHospitals(doctorId)
    }
}
