package com.example.cloudcareapp.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudcareapp.data.model.*
import com.example.cloudcareapp.data.repository.MockDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: MockDataRepository = MockDataRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _uiState.value = DashboardUiState.Loading
                
                val patient = repository.getPatient(7)
                val stats = repository.getDashboardStats()
                val activities = repository.getRecentActivities()
                
                _uiState.value = DashboardUiState.Success(
                    patient = patient,
                    stats = stats,
                    recentActivities = activities
                )
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val patient: Patient,
        val stats: DashboardStats,
        val recentActivities: List<Activity>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
