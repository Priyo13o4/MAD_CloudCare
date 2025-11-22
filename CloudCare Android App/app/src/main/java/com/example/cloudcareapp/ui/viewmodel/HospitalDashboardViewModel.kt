package com.example.cloudcareapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.HospitalDashboardStats
import com.example.cloudcareapp.data.repository.HospitalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HospitalDashboardViewModel : ViewModel() {
    private val repository = HospitalRepository()

    // Always fetch live data - no cache initialization
    private val _stats = MutableStateFlow<HospitalDashboardStats?>(null)
    val stats: StateFlow<HospitalDashboardStats?> = _stats.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _hospitalName = MutableStateFlow("")
    val hospitalName: StateFlow<String> = _hospitalName.asStateFlow()

    private val _hospitalLocation = MutableStateFlow("")
    val hospitalLocation: StateFlow<String> = _hospitalLocation.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val hospitalId = AppDataCache.getHospitalId()
                if (hospitalId != null) {
                    // Fetch Profile for Name/Location
                    val profile = repository.getHospitalProfile(hospitalId)
                    _hospitalName.value = profile.name
                    _hospitalLocation.value = "${profile.city ?: ""}, ${profile.state ?: ""}"

                    // Fetch Stats
                    val dashboardStats = repository.getDashboardStats(hospitalId)
                    _stats.value = dashboardStats
                } else {
                    _error.value = "Hospital ID not found. Please login again."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load dashboard data"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
