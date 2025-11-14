package com.example.cloudcareapp.ui.screens.wearables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudcareapp.data.model.*
import com.example.cloudcareapp.data.repository.MockDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WearablesViewModel(
    private val repository: MockDataRepository = MockDataRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<WearablesUiState>(WearablesUiState.Loading)
    val uiState: StateFlow<WearablesUiState> = _uiState.asStateFlow()
    
    init {
        loadWearablesData()
    }
    
    fun loadWearablesData() {
        viewModelScope.launch {
            try {
                _uiState.value = WearablesUiState.Loading
                
                val devices = repository.getWearableDevices()
                val healthSummary = repository.getHealthSummary()
                val insights = repository.getHealthInsights()
                
                _uiState.value = WearablesUiState.Success(
                    devices = devices,
                    healthSummary = healthSummary,
                    insights = insights
                )
            } catch (e: Exception) {
                _uiState.value = WearablesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class WearablesUiState {
    object Loading : WearablesUiState()
    data class Success(
        val devices: List<WearableDevice>,
        val healthSummary: HealthSummary,
        val insights: List<HealthInsight>
    ) : WearablesUiState()
    data class Error(val message: String) : WearablesUiState()
}
