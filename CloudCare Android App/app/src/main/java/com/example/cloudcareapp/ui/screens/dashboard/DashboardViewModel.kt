package com.example.cloudcareapp.ui.screens.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.*
import com.example.cloudcareapp.data.repository.HealthMetricsRepository
import com.example.cloudcareapp.data.repository.MockDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class DashboardViewModel(
    private val healthMetricsRepository: HealthMetricsRepository = HealthMetricsRepository(),
    private val mockRepository: MockDataRepository = MockDataRepository
) : ViewModel() {
    
    companion object {
        // Patient ID from iOS CloudSync app with 27,185+ metrics in backend
        const val PATIENT_ID = "3228128A-7110-4D47-8EDB-3A9160E3808A"
        private const val TAG = "DashboardViewModel"
    }

    private data class PrefetchRequest(val period: String, val days: Int)
    private val prefetchQueue = listOf(
        PrefetchRequest(period = "hourly", days = 1),
        PrefetchRequest(period = "daily", days = 7),
        PrefetchRequest(period = "daily", days = 30),
        PrefetchRequest(period = "weekly", days = 90)
    )
    
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    val lastSyncTime: StateFlow<LocalDateTime?> = AppDataCache.lastSyncTime
    val isSyncing: StateFlow<Boolean> = AppDataCache.isSyncing

    private var prefetchJob: Job? = null
    
    init {
        loadDashboardData()
    }
    
    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                // If cache has data, use it immediately and return (skip loading state)
                if (AppDataCache.hasTodaySummary()) {
                    Log.d(TAG, "Using cached dashboard data")
                    val cachedSummary = AppDataCache.getTodaySummary()!!
                    val healthSummary = mapToHealthSummary(cachedSummary.summary)
                    
                    val patient = mockRepository.getPatient(7)
                    val stats = mockRepository.getDashboardStats()
                    val activities = mockRepository.getRecentActivities()
                    
                    _uiState.value = DashboardUiState.Success(
                        patient = patient,
                        stats = stats,
                        recentActivities = activities,
                        healthSummary = healthSummary,
                        isUsingRealData = true
                    )
                    prefetchWearablesData()
                    return@launch
                }
                
                // First time load - show loading state
                _uiState.value = DashboardUiState.Loading
                
                // Fetch real health data from backend
                val summaryResult = healthMetricsRepository.getTodaySummary(PATIENT_ID)
                
                summaryResult.onSuccess { todayResponse ->
                    // Cache the response
                    AppDataCache.setTodaySummary(todayResponse)
                    AppDataCache.updateLastSyncTime()
                    
                    // Map backend data to UI models
                    val healthSummary = mapToHealthSummary(todayResponse.summary)
                    
                    // Use mock data for patient info and activities (not yet implemented in backend)
                    val patient = mockRepository.getPatient(7)
                    val stats = mockRepository.getDashboardStats()
                    val activities = mockRepository.getRecentActivities()
                    
                    _uiState.value = DashboardUiState.Success(
                        patient = patient,
                        stats = stats,
                        recentActivities = activities,
                        healthSummary = healthSummary,
                        isUsingRealData = true
                    )
                    prefetchWearablesData()
                }.onFailure { error ->
                    Log.e(TAG, "Failed to fetch dashboard data", error)
                    // Try to use cached data first
                    if (AppDataCache.hasTodaySummary()) {
                        Log.d(TAG, "Falling back to cached data after network error")
                        val cachedSummary = AppDataCache.getTodaySummary()!!
                        val healthSummary = mapToHealthSummary(cachedSummary.summary)
                        
                        val patient = mockRepository.getPatient(7)
                        val stats = mockRepository.getDashboardStats()
                        val activities = mockRepository.getRecentActivities()
                        
                        _uiState.value = DashboardUiState.Success(
                            patient = patient,
                            stats = stats,
                            recentActivities = activities,
                            healthSummary = healthSummary,
                            isUsingRealData = true,
                            errorMessage = "Showing cached data. ${error.message}"
                        )
                    } else {
                        // Fallback to mock data if no cache available
                        Log.d(TAG, "Using mock data as final fallback")
                        val patient = mockRepository.getPatient(7)
                        val stats = mockRepository.getDashboardStats()
                        val activities = mockRepository.getRecentActivities()
                        val mockHealthSummary = mockRepository.getHealthSummary()
                        
                        _uiState.value = DashboardUiState.Success(
                            patient = patient,
                            stats = stats,
                            recentActivities = activities,
                            healthSummary = mockHealthSummary,
                            isUsingRealData = false,
                            errorMessage = "Using cached data. ${error.message}"
                        )
                    }
                    prefetchWearablesData()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading dashboard", e)
                // Try cached data before showing error
                if (AppDataCache.hasTodaySummary()) {
                    Log.d(TAG, "Exception occurred, falling back to cached data")
                    val cachedSummary = AppDataCache.getTodaySummary()!!
                    val healthSummary = mapToHealthSummary(cachedSummary.summary)
                    
                    val patient = mockRepository.getPatient(7)
                    val stats = mockRepository.getDashboardStats()
                    val activities = mockRepository.getRecentActivities()
                    
                    _uiState.value = DashboardUiState.Success(
                        patient = patient,
                        stats = stats,
                        recentActivities = activities,
                        healthSummary = healthSummary,
                        isUsingRealData = true,
                        errorMessage = "Showing cached data. Error: ${e.message}"
                    )
                } else {
                    _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            try {
                AppDataCache.setSyncing(true)
                
                // Force fresh fetch from backend
                val summaryResult = healthMetricsRepository.getTodaySummary(PATIENT_ID)
                
                summaryResult.onSuccess { todayResponse ->
                    // Update cache
                    AppDataCache.setTodaySummary(todayResponse)
                    AppDataCache.updateLastSyncTime()
                    
                    Log.d(TAG, "Dashboard refresh successful")
                    
                    // Map backend data to UI models
                    val healthSummary = mapToHealthSummary(todayResponse.summary)
                    
                    // Use mock data for patient info and activities
                    val patient = mockRepository.getPatient(7)
                    val stats = mockRepository.getDashboardStats()
                    val activities = mockRepository.getRecentActivities()
                    
                    _uiState.value = DashboardUiState.Success(
                        patient = patient,
                        stats = stats,
                        recentActivities = activities,
                        healthSummary = healthSummary,
                        isUsingRealData = true
                    )
                    prefetchWearablesData()
                }.onFailure { error ->
                    Log.e(TAG, "Refresh failed", error)
                    // Keep showing current state but with error indicator
                    if (_uiState.value is DashboardUiState.Success) {
                        val currentState = _uiState.value as DashboardUiState.Success
                        _uiState.value = currentState.copy(
                            errorMessage = "Refresh failed: ${error.message}"
                        )
                    }
                }
                
                AppDataCache.setSyncing(false)
            } catch (e: Exception) {
                Log.e(TAG, "Exception during refresh", e)
                AppDataCache.setSyncing(false)
            }
        }
    }

    private fun prefetchWearablesData() {
        if (prefetchJob?.isActive == true) return
        prefetchJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!AppDataCache.hasDevices()) {
                    val devicesResult = healthMetricsRepository.getPairedDevices(PATIENT_ID)
                    devicesResult.onSuccess { devices ->
                        if (devices.isNotEmpty()) {
                            AppDataCache.setDevices(devices)
                            AppDataCache.updateLastSyncTime()
                        }
                    }.onFailure { error ->
                        Log.w(TAG, "Device prefetch failed", error)
                    }
                    delay(150L)
                }

                prefetchQueue.forEach { request ->
                    if (!AppDataCache.hasMetrics(request.period, request.days)) {
                        val metricsResult = healthMetricsRepository.getAggregatedMetrics(
                            patientId = PATIENT_ID,
                            period = request.period,
                            days = request.days
                        )
                        metricsResult.onSuccess { response ->
                            AppDataCache.setMetrics(request.period, request.days, response)
                            AppDataCache.updateLastSyncTime()
                        }.onFailure { error ->
                            Log.w(TAG, "Metrics prefetch failed for ${request.period}_${request.days}", error)
                        }
                        delay(150L)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Wearables prefetch failed", e)
            }
        }
    }
    
    /**
     * Map backend TodaySummary to UI HealthSummary model
     */
    private fun mapToHealthSummary(summary: TodaySummary): HealthSummary {
        // Extract values with defaults
        val steps = summary.steps.total?.toInt() ?: 0
        val stepsChange = parseChangePercentage(summary.steps.change)
        
        val heartRate = summary.heart_rate.avg?.toInt() ?: 0
        val heartRateStatus = when {
            heartRate == 0 -> "No data"
            heartRate < 60 -> "Low"
            heartRate > 100 -> "Elevated"
            else -> "Normal"
        }
        
        val calories = summary.calories.total?.toInt() ?: 0
        val caloriesGoal = 2000
        val caloriesPercentage = if (caloriesGoal > 0) {
            ((calories.toFloat() / caloriesGoal) * 100).toInt()
        } else 0
        
        // Sleep data from backend - now includes time_in_bed, time_asleep, and stage breakdown
        val sleepHours = summary.sleep?.time_asleep ?: 0.0
        val sleepChange = parseChangePercentage(summary.sleep?.change)
        
        // Extract new sleep data structure
        val sleepTimeInBed = summary.sleep?.time_in_bed
        val sleepTimeAsleep = summary.sleep?.time_asleep
        val sleepStages = summary.sleep?.stages
        val sleepSessions = summary.sleep?.sessions
        
        return HealthSummary(
            steps = steps,
            stepsChange = stepsChange,
            heartRate = heartRate,
            heartRateStatus = heartRateStatus,
            sleepHours = sleepHours,
            sleepChange = sleepChange,
            calories = calories,
            caloriesPercentage = caloriesPercentage,
            caloriesGoal = caloriesGoal,
            sleepTimeInBed = sleepTimeInBed,
            sleepTimeAsleep = sleepTimeAsleep,
            sleepStages = sleepStages,
            sleepSessions = sleepSessions
        )
    }
    
    /**
     * Parse percentage change string like "+12%" to integer 12
     */
    private fun parseChangePercentage(change: String?): Int {
        if (change == null) return 0
        return try {
            change.replace("%", "").replace("+", "").toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    
    data class Success(
        val patient: Patient,
        val stats: DashboardStats,
        val recentActivities: List<Activity>,
        val healthSummary: HealthSummary? = null,
        val isUsingRealData: Boolean = false,
        val errorMessage: String? = null
    ) : DashboardUiState()
    
    data class Error(val message: String) : DashboardUiState()
}

