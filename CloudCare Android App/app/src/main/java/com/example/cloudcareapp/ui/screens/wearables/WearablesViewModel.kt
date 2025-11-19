package com.example.cloudcareapp.ui.screens.wearables

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.*
import com.example.cloudcareapp.data.repository.HealthMetricsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class WearablesViewModel(
    private val healthMetricsRepository: HealthMetricsRepository = HealthMetricsRepository()
) : ViewModel() {
    
    companion object {
        const val PATIENT_ID = "3228128A-7110-4D47-8EDB-3A9160E3808A"
        private const val TAG = "WearablesViewModel"
    }
    
    private val _uiState = MutableStateFlow<WearablesUiState>(WearablesUiState.Loading)
    val uiState: StateFlow<WearablesUiState> = _uiState.asStateFlow()
    
    private val _weeklyData = MutableStateFlow<WeeklyDataState>(WeeklyDataState.Loading)
    val weeklyData: StateFlow<WeeklyDataState> = _weeklyData.asStateFlow()
    
    private val _sleepTrends = MutableStateFlow<List<SleepTrendDataPoint>>(emptyList())
    val sleepTrends: StateFlow<List<SleepTrendDataPoint>> = _sleepTrends.asStateFlow()
    
    private val _heartRateTrends = MutableStateFlow<List<HeartRateTrendDataPoint>>(emptyList())
    val heartRateTrends: StateFlow<List<HeartRateTrendDataPoint>> = _heartRateTrends.asStateFlow()
    
    private val _selectedDateRange = MutableStateFlow(30) // CHANGE 1: Load 30 days by default so "Monthly" tabs work immediately
    val selectedDateRange: StateFlow<Int> = _selectedDateRange.asStateFlow()
    
    private val _selectedPeriod = MutableStateFlow("daily") // hourly for 1D, daily for others
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()
    
    val lastSyncTime: StateFlow<LocalDateTime?> = AppDataCache.lastSyncTime
    val isSyncing: StateFlow<Boolean> = AppDataCache.isSyncing
    
    init {
        observeCachedWearables()
        observeWeeklyMetrics()
        loadWearablesData()
        loadWeeklyTrends()
        loadSleepTrends(30) // CHANGE 2: Pass 30 here explicitly
        loadHeartRateTrends(30)
    }
    
    fun setDateRange(days: Int) {
        _selectedDateRange.value = days
        _selectedPeriod.value = if (days == 1) "hourly" else "daily"
        loadWeeklyTrends()
        loadSleepTrends(days) // Reload sleep trends with new range
        loadHeartRateTrends(days) // Reload heart rate trends with new range
    }    private fun observeCachedWearables() {
        viewModelScope.launch {
            combine(
                AppDataCache.todaySummaryCache,
                AppDataCache.devicesCache
            ) { summaryResponse, devices ->
                // Only emit if we have both summary AND devices (devices can be empty list, but should be explicitly set)
                if (summaryResponse != null) {
                    Log.d(TAG, "Emitting cached data: summary present, devices=${devices.size}")
                    summaryResponse to devices
                } else {
                    null
                }
            }
                .filterNotNull()
                .collect { (summaryResponse, devices) ->
                    Log.d(TAG, "Cache observer triggered - updating UI with cached data")
                    val healthSummary = mapToHealthSummary(summaryResponse.summary)
                    val insights = generateInsights(healthSummary)
                    _uiState.value = WearablesUiState.Success(
                        devices = devices,
                        healthSummary = healthSummary,
                        insights = insights,
                        isUsingRealData = true
                    )
                }
        }
    }

    private fun observeWeeklyMetrics() {
        viewModelScope.launch {
            combine(
                AppDataCache.metricsCache,
                _selectedPeriod,
                _selectedDateRange
            ) { metricsMap, period, days ->
                metricsMap[metricCacheKey(period, days)]
            }
                .filterNotNull()
                .collect { response ->
                    _weeklyData.value = WeeklyDataState.Success(response)
                }
        }
    }
    
    fun loadWearablesData() {
        viewModelScope.launch {
            try {
                val hasSummary = AppDataCache.hasTodaySummary()
                val hasDevices = AppDataCache.hasDevices()
                
                if (hasSummary && hasDevices) {
                    Log.d(TAG, "Wearables data satisfied from cache")
                    return@launch
                }
                
                // Only show loading if we have no cache at all
                if (!hasSummary && !hasDevices) {
                    _uiState.value = WearablesUiState.Loading
                }
                
                var loadedSummary = false
                var loadedDevices = false
                
                if (!hasSummary) {
                    val summaryResult = healthMetricsRepository.getTodaySummary(PATIENT_ID)
                    summaryResult.onSuccess { todayResponse ->
                        AppDataCache.setTodaySummary(todayResponse)
                        AppDataCache.updateLastSyncTime()
                        loadedSummary = true
                    }.onFailure { error ->
                        Log.e(TAG, "Failed to load wearables summary", error)
                        // IMPORTANT: Do NOT show error if cache exists - silently fall back
                        Log.d(TAG, "Falling back to cached summary due to network error")
                    }
                } else {
                    loadedSummary = true
                }
                
                if (!hasDevices) {
                    val devicesResult = healthMetricsRepository.getPairedDevices(PATIENT_ID)
                    devicesResult.onSuccess { devices ->
                        AppDataCache.setDevices(devices)
                        AppDataCache.updateLastSyncTime()
                        loadedDevices = true
                    }.onFailure { error ->
                        Log.e(TAG, "Failed to load devices", error)
                        // IMPORTANT: Do NOT show error if cache exists - silently fall back
                        Log.d(TAG, "Falling back to cached devices due to network error")
                    }
                } else {
                    loadedDevices = true
                }
                
                // Check if we have any data (cached or newly loaded) to display
                if ((loadedSummary || AppDataCache.hasTodaySummary()) && 
                    (loadedDevices || AppDataCache.hasDevices())) {
                    Log.d(TAG, "Have data to display (cached or loaded)")
                    // Cache observer will update UI automatically
                } else if (!AppDataCache.hasTodaySummary() || !AppDataCache.hasDevices()) {
                    // Only show error if we have NO cache AND failed to load
                    _uiState.value = WearablesUiState.Error(
                        "Unable to load wearables data. Please check your connection."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading wearables", e)
                if (!AppDataCache.hasTodaySummary() || !AppDataCache.hasDevices()) {
                    _uiState.value = WearablesUiState.Error(e.message ?: "Unknown error")
                } else {
                    Log.d(TAG, "Exception occurred, but have cached data to display")
                }
            }
        }
    }
    
    fun loadWeeklyTrends() {
        viewModelScope.launch {
            try {
                val cacheKey = metricCacheKey(_selectedPeriod.value, _selectedDateRange.value)

                val cachedMetrics = AppDataCache.getMetrics(_selectedPeriod.value, _selectedDateRange.value)
                if (cachedMetrics != null) {
                    Log.d(TAG, "Using cached metrics for $cacheKey")
                    _weeklyData.value = WeeklyDataState.Success(cachedMetrics)
                    return@launch
                }

                _weeklyData.value = WeeklyDataState.Loading

                val result = healthMetricsRepository.getAggregatedMetrics(
                    patientId = PATIENT_ID,
                    period = _selectedPeriod.value,
                    days = _selectedDateRange.value
                )

                result.onSuccess { response ->
                    AppDataCache.setMetrics(_selectedPeriod.value, _selectedDateRange.value, response)
                    AppDataCache.updateLastSyncTime()
                }.onFailure { error ->
                    Log.e(TAG, "Failed to load weekly data", error)
                    // Try to use older cached data as fallback
                    val fallbackMetrics = AppDataCache.getMetrics(_selectedPeriod.value, _selectedDateRange.value)
                    if (fallbackMetrics != null) {
                        Log.d(TAG, "Using fallback cached metrics after network error")
                        _weeklyData.value = WeeklyDataState.Success(fallbackMetrics)
                    } else {
                        _weeklyData.value = WeeklyDataState.Error(error.message ?: "Failed to load weekly data")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading weekly trends", e)
                // Try to use cached data
                val cachedMetrics = AppDataCache.getMetrics(_selectedPeriod.value, _selectedDateRange.value)
                if (cachedMetrics != null) {
                    Log.d(TAG, "Using cached metrics after exception")
                    _weeklyData.value = WeeklyDataState.Success(cachedMetrics)
                } else {
                    _weeklyData.value = WeeklyDataState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }
    
    /**
     * Load accurate sleep trends using the dedicated sleep-trends endpoint.
     * This separates time_in_bed from time_asleep for honest visualization.
     */
    fun loadSleepTrends(days: Int = 7) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading sleep trends for $days days")
                val result = healthMetricsRepository.getSleepTrends(
                    patientId = PATIENT_ID,
                    days = days
                )
                
                result.onSuccess { response ->
                    _sleepTrends.value = response.data
                    Log.d(TAG, "Sleep trends loaded: ${response.data.size} data points")
                }.onFailure { error ->
                    Log.e(TAG, "Failed to load sleep trends", error)
                    _sleepTrends.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading sleep trends", e)
                _sleepTrends.value = emptyList()
            }
        }
    }
    
    /**
     * Load heart rate trends using the dedicated heart-rate-trends endpoint.
     * This returns daily average/min/max BPM with 72 BPM as baseline for visualization.
     */
    fun loadHeartRateTrends(days: Int = 7) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading heart rate trends for $days days")
                val result = healthMetricsRepository.getHeartRateTrends(
                    patientId = PATIENT_ID,
                    days = days
                )
                
                result.onSuccess { response ->
                    _heartRateTrends.value = response.data
                    Log.d(TAG, "Heart rate trends loaded: ${response.data.size} data points")
                }.onFailure { error ->
                    Log.e(TAG, "Failed to load heart rate trends", error)
                    _heartRateTrends.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading heart rate trends", e)
                _heartRateTrends.value = emptyList()
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            try {
                AppDataCache.setSyncing(true)
                
                val summaryResult = healthMetricsRepository.getTodaySummary(PATIENT_ID)
                summaryResult.onSuccess { todayResponse ->
                    AppDataCache.setTodaySummary(todayResponse)
                    AppDataCache.updateLastSyncTime()
                    Log.d(TAG, "Wearables summary refreshed")
                }.onFailure { error ->
                    Log.e(TAG, "Refresh summary failed", error)
                    // IMPORTANT: Do NOT show error - cache will be shown with warning indicator
                    Log.d(TAG, "Summary refresh failed, will show cached data")
                }

                val devicesResult = healthMetricsRepository.getPairedDevices(PATIENT_ID)
                devicesResult.onSuccess { devices ->
                    AppDataCache.setDevices(devices)
                    AppDataCache.updateLastSyncTime()
                }.onFailure { error ->
                    Log.e(TAG, "Refresh devices failed", error)
                    // IMPORTANT: Do NOT show error - cache will be shown with warning indicator
                    Log.d(TAG, "Devices refresh failed, will show cached data")
                }

                val metricsResult = healthMetricsRepository.getAggregatedMetrics(
                    patientId = PATIENT_ID,
                    period = _selectedPeriod.value,
                    days = _selectedDateRange.value
                )

                metricsResult.onSuccess { response ->
                    AppDataCache.setMetrics(_selectedPeriod.value, _selectedDateRange.value, response)
                    AppDataCache.updateLastSyncTime()
                }.onFailure { error ->
                    Log.e(TAG, "Metrics refresh failed", error)
                    // IMPORTANT: Do NOT show error - cache will handle the display
                    Log.d(TAG, "Metrics refresh failed, will show cached data if available")
                }
                
                Log.d(TAG, "Refresh completed - showing cached data if network failed")
            } catch (e: Exception) {
                Log.e(TAG, "Exception during refresh", e)
                // IMPORTANT: Never throw error - silently continue
                Log.d(TAG, "Exception during refresh, will show cached data")
            } finally {
                AppDataCache.setSyncing(false)
            }
        }
    }
    
    fun pairDevice(pairingData: PairingData, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = PairingRequest(
                    userId = pairingData.userId,
                    deviceId = pairingData.deviceId,
                    deviceName = pairingData.deviceName,
                    deviceType = pairingData.deviceType,
                    generatedAt = pairingData.generatedAt,
                    expiresAt = pairingData.expiresAt,
                    pairingCode = pairingData.pairingCode,
                    androidUserId = PATIENT_ID
                )
                
                val result = healthMetricsRepository.pairDevice(request)
                
                result.onSuccess { response ->
                    // Backend returns 201 on success with pairing details
                    onResult(true, response.message)
                    // Reload devices after successful pairing
                    val devicesResult = healthMetricsRepository.getPairedDevices(PATIENT_ID)
                    devicesResult.onSuccess { devices ->
                        AppDataCache.setDevices(devices)
                        AppDataCache.updateLastSyncTime()
                    }.onFailure { error ->
                        Log.e(TAG, "Failed to refresh devices after pairing", error)
                    }
                }.onFailure { error ->
                    onResult(false, error.message ?: "Network error")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }
    
    private fun mapToHealthSummary(summary: TodaySummary): HealthSummary {
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
        
        // Extract sleep data - using time_asleep as primary metric for sleepHours
        val sleepTimeAsleep = summary.sleep?.time_asleep ?: 0.0
        val sleepTimeInBed = summary.sleep?.time_in_bed ?: 0.0
        val sleepHours = sleepTimeAsleep  // Use time_asleep as the primary sleep hours metric
        val sleepChange = parseChangePercentage(summary.sleep?.change)
        val sleepStages = summary.sleep?.stages  // Get stage breakdown
        val sleepSessions = summary.sleep?.sessions  // Get individual sleep sessions
        
        Log.d(TAG, "Sleep data extracted: timeInBed=$sleepTimeInBed, timeAsleep=$sleepHours, sessions=${sleepSessions?.size ?: 0}")
        
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
    
    private fun generateInsights(summary: HealthSummary): List<HealthInsight> {
        val insights = mutableListOf<HealthInsight>()
        insights += HealthInsight(
            id = 1,
            title = if (summary.stepsChange >= 0) "Steps trending up" else "Steps dipped today",
            value = "${summary.steps} steps",
            subtitle = "vs yesterday ${formatChange(summary.stepsChange)}",
            type = InsightType.STEPS,
            trend = formatChange(summary.stepsChange)
        )
        insights += HealthInsight(
            id = 2,
            title = "Heart rate ${summary.heartRateStatus}",
            value = "${summary.heartRate} bpm avg",
            subtitle = summary.heartRateStatus,
            type = InsightType.HEART_RATE
        )
        insights += HealthInsight(
            id = 3,
            title = "Sleep duration",
            value = String.format("%.1f h", summary.sleepHours),
            subtitle = "vs yesterday ${formatChange(summary.sleepChange)}",
            type = InsightType.SLEEP,
            trend = formatChange(summary.sleepChange)
        )
        insights += HealthInsight(
            id = 4,
            title = "Calories goal",
            value = "${summary.calories}/${summary.caloriesGoal} kcal",
            subtitle = "${summary.caloriesPercentage}% of goal",
            type = InsightType.CALORIES
        )
        return insights
    }

    private fun parseChangePercentage(change: String?): Int {
        if (change == null) return 0
        return try {
            change.replace("%", "").replace("+", "").toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun formatChange(change: Int?): String {
        if (change == null) return "0%"
        return when {
            change > 0 -> "+${change}%"
            change < 0 -> "${change}%"
            else -> "0%"
        }
    }

    private fun metricCacheKey(period: String, days: Int) = "${period}_${days}"
}

sealed class WearablesUiState {
    object Loading : WearablesUiState()
    data class Success(
        val devices: List<WearableDevice>,
        val healthSummary: HealthSummary,
        val insights: List<HealthInsight>,
        val isUsingRealData: Boolean = false
    ) : WearablesUiState()
    data class Error(val message: String) : WearablesUiState()
}

sealed class WeeklyDataState {
    object Loading : WeeklyDataState()
    data class Success(val data: AggregatedMetricsResponse) : WeeklyDataState()
    data class Error(val message: String) : WeeklyDataState()
}
