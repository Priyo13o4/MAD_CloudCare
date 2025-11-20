package com.example.cloudcareapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.DoctorPatientResponse
import com.example.cloudcareapp.data.model.DoctorProfileResponse
import com.example.cloudcareapp.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class DoctorProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = com.example.cloudcareapp.data.repository.DoctorRepository()

    private val _doctorProfile = MutableLiveData<DoctorProfileResponse?>()
    val doctorProfile: LiveData<DoctorProfileResponse?> = _doctorProfile

    private val _patients = MutableLiveData<List<DoctorPatientResponse>>(emptyList())
    val patients: LiveData<List<DoctorPatientResponse>> = _patients

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
    
    fun refresh(doctorId: String) {
        loadDoctorProfile(doctorId)
        loadDoctorPatients(doctorId)
    }
}
