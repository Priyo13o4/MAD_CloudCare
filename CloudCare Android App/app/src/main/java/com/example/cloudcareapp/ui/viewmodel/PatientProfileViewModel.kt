package com.example.cloudcareapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cloudcareapp.data.model.PatientProfileData
import com.example.cloudcareapp.data.repository.PatientRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for patient profile with caching support
 */
class PatientProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = PatientRepository(application.applicationContext)
    
    private val _profileState = MutableLiveData<ProfileState>()
    val profileState: LiveData<ProfileState> = _profileState
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage
    
    sealed class ProfileState {
        object Idle : ProfileState()
        object Loading : ProfileState()
        data class Success(val profile: PatientProfileData) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }
    
    /**
     * Load patient profile
     * Uses cache if available and valid
     */
    fun loadProfile(patientId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.getPatientProfile(patientId, forceRefresh)
            
            _isLoading.value = false
            
            result.onSuccess { profile ->
                _profileState.value = ProfileState.Success(profile)
                _errorMessage.value = null
            }.onFailure { error ->
                val message = error.message ?: "Failed to load profile"
                _errorMessage.value = message
                _profileState.value = ProfileState.Error(message)
            }
        }
    }
    
    /**
     * Refresh profile (force API call)
     */
    fun refreshProfile(patientId: String) {
        loadProfile(patientId, forceRefresh = true)
    }
    
    /**
     * Clear cached profile
     */
    fun clearCache() {
        repository.clearCache()
    }
    
    /**
     * Check if profile is cached
     */
    fun hasCache(): Boolean {
        return repository.hasCache()
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
