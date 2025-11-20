package com.example.cloudcareapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.HospitalProfileResponse
import com.example.cloudcareapp.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class HospitalProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = com.example.cloudcareapp.data.repository.HospitalRepository()
    
    private val _hospitalProfile = MutableLiveData<HospitalProfileResponse?>()
    val hospitalProfile: LiveData<HospitalProfileResponse?> = _hospitalProfile
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    
    fun loadHospitalProfile(hospitalId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.getHospitalProfile(hospitalId)
                _hospitalProfile.value = response
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load hospital profile"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refresh(hospitalId: String) {
        loadHospitalProfile(hospitalId)
    }
}
