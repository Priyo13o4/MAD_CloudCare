package com.example.cloudcareapp.ui.screens.wearables

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.*
import com.example.cloudcareapp.data.repository.HealthMetricsRepository
import com.example.cloudcareapp.utils.formatDateWithTimezone
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime

class WearablesViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val healthMetricsRepository: HealthMetricsRepository = HealthMetricsRepository()
    private val gson = Gson()
    
    companion object {
        const val PATIENT_ID = "3228128A-7110-4D47-8EDB-3A9160E3808A"
        private const val TAG = "WearablesViewModel"
        private const val CACHE_FILE_SUMMARY = "cache_summary.json"
        private const val CACHE_FILE_DEVICES = "cache_devices.json"
        private const val CACHE_FILE_METRICS = "cache_metrics.json"
        private const val CACHE_FILE_SLEEP = "cache_sleep.json"
        private const val CACHE_FILE_HEART = "cache_heart.json"
    }
    
    private val _uiState = MutableStateFlow<WearablesUiState>(WearablesUiState.Loading)
    val uiState: StateFlow<WearablesUiState> = _uiState.asStateFlow()
    
    private val _weeklyData = MutableStateFlow<WeeklyDataState>(WeeklyDataState.Loading)
    val weeklyData: StateFlow<WeeklyDataState> = _weeklyData.asStateFlow()

    // Decoupled states for each card
    private val _stepsData = MutableStateFlow<List<AggregatedDataPoint>>(emptyList())
    val stepsData: StateFlow<List<AggregatedDataPoint>> = _stepsData.asStateFlow()

    private val _caloriesData = MutableStateFlow<List<AggregatedDataPoint>>(emptyList())
    val caloriesData: StateFlow<List<AggregatedDataPoint>> = _caloriesData.asStateFlow()
    
    private val _sleepTrends = MutableStateFlow<List<SleepTrendDataPoint>>(emptyList())
    val sleepTrends: StateFlow<List<SleepTrendDataPoint>> = _sleepTrends.asStateFlow()

    private val _dailySleepStages = MutableStateFlow<SleepStages?>(null)
    val dailySleepStages: StateFlow<SleepStages?> = _dailySleepStages.asStateFlow()
    
    private val _heartRateTrends = MutableStateFlow<List<HeartRateTrendDataPoint>>(emptyList())
    val heartRateTrends: StateFlow<List<HeartRateTrendDataPoint>> = _heartRateTrends.asStateFlow()
    
    private val _selectedDateRange = MutableStateFlow(30) // CHANGE 1: Load 30 days by default so "Monthly" tabs work immediately
    val selectedDateRange: StateFlow<Int> = _selectedDateRange.asStateFlow()
    
    private val _selectedPeriod = MutableStateFlow("daily") // hourly for 1D, daily for others
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()
    
    private val _hasInitializedCache = MutableStateFlow(false)
    val hasInitializedCache: StateFlow<Boolean> = _hasInitializedCache.asStateFlow()

    // Local cache of trend data per metric + timeframe (D/W/M)
    private val metricTrendsCache = mutableMapOf<String, MutableMap<String, List<AggregatedDataPoint>>>()
    private val heartRateTrendsCache = mutableMapOf<String, List<HeartRateTrendDataPoint>>()
    
    val lastSyncTime: StateFlow<LocalDateTime?> = AppDataCache.lastSyncTime
    val isSyncing: StateFlow<Boolean> = AppDataCache.isSyncing
    
    init {
        loadCacheFromDisk()
        observeCachedWearables()
        // ⚠️ IMPORTANT: Do NOT call fetchComprehensiveMetrics() here
        // It will be called ONCE from login screen or dashboard
        // This prevents redundant API calls when opening wearables screen
    }
    
    /**
     * Initialize cache on first login or app startup
     * Called ONCE from Dashboard/Login screen
     * Fetches all data and caches it for reuse across screens
     * 
     * If cache already exists, uses cached data instead of making API call
     */
    fun initializeCache(forceRefresh: Boolean = false) {
        // Skip if already initialized and not forcing refresh
        if (_hasInitializedCache.value && !forceRefresh) {
            Log.d(TAG, "Cache already initialized, skipping init")
            return
        }
        
        viewModelScope.launch {
            try {
                // Check if we have valid cache - if so, process it into StateFlow values
                if (AppDataCache.hasTodaySummary() && AppDataCache.hasComprehensiveMetrics() && !forceRefresh) {
                    Log.d(TAG, "Using existing cache data")
                    val cachedMetrics = AppDataCache.getComprehensiveMetrics()
                    if (cachedMetrics != null) {
                        Log.d(TAG, "Processing cached comprehensive metrics into StateFlow values")
                        processComprehensiveResponse(cachedMetrics)
                    }
                    _hasInitializedCache.value = true
                    return@launch
                }
                
                // Fetch fresh data from API
                val success = fetchComprehensiveMetrics()
                if (success) {
                    preloadHourlyMetricsIfNeeded()
                }
                _hasInitializedCache.value = success || AppDataCache.hasComprehensiveMetrics()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing cache", e)
                _hasInitializedCache.value = false
            }
        }
    }
    
    /**
     * Ensure cache is initialized (call from screens)
     * This loads cached metrics from AppDataCache into StateFlow values
     */
    fun ensureCacheInitialized() {
        // If cache is already populated in memory, skip
        if (_stepsData.value.isNotEmpty()) {
            Log.d(TAG, "Cache already loaded in memory")
            return
        }
        
        // If we have comprehensive metrics cached, load them into StateFlow values
        val cachedComprehensiveMetrics = AppDataCache.getComprehensiveMetrics()
        if (cachedComprehensiveMetrics != null) {
            Log.d(TAG, "Loading cached comprehensive metrics from AppDataCache")
            processComprehensiveResponse(cachedComprehensiveMetrics)
            _hasInitializedCache.value = true
            return
        }
        
        // Otherwise, initialize cache (will either load from disk or fetch from network)
        Log.d(TAG, "Cache not in memory, initializing...")
        initializeCache(forceRefresh = false)
    }
    
    /**
     * ========================================
     * 🚀 COMPREHENSIVE METRICS LOADER
     * ========================================
     * 
     * Load ALL health data in a SINGLE API call:
     * - Today's summary for all metrics
     * - 30-day time-series data for all charts
     * - Device sync information
     * 
    * This replaces individual loadStepsTrend(), loadCaloriesTrend(), loadSleepTrends(), 
    * and loadHeartRateTrends() calls to prevent race conditions and card bugs.
     * 
     * Data is cached in AppDataCache and reused across all screens
     */
    private suspend fun fetchComprehensiveMetrics(days: Int = 30): Boolean {
        return try {
            Log.d(TAG, "Loading comprehensive metrics (ALL data in one call)")
            val result = healthMetricsRepository.getComprehensiveMetrics(
                patientId = PATIENT_ID,
                days = days
            )

            var success = false
            result.onSuccess { response ->
                Log.d(TAG, "Comprehensive metrics loaded successfully!")
                processComprehensiveResponse(response)
                fetchTodaySummaryAndDevices()
                saveCacheToDisk()
                success = true
            }.onFailure { error ->
                Log.e(TAG, "Failed to load comprehensive metrics", error)
                Log.d(TAG, "Using cached data after comprehensive fetch failed")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Exception loading comprehensive metrics", e)
            false
        }
    }

    private fun processComprehensiveResponse(response: ComprehensiveMetricsResponse) {
        Log.d(TAG, "Processing comprehensive response...")
        
        // ===== STEPS DATA =====
        val stepsPoints = response.time_series.steps.map { point ->
            AggregatedDataPoint(
                date = point.date.formatDateWithTimezone("MMM dd"),
                total = point.total ?: 0.0,
                avg = point.avg ?: 0.0,
                min = point.min ?: 0.0,
                max = point.max ?: 0.0,
                count = point.count ?: 0,
                unit = point.unit ?: "steps"
            )
        }
        Log.d(TAG, "Steps data: ${stepsPoints.size} daily points")
        
        cacheTrendData("steps", "M", stepsPoints.takeLast(30))
        cacheTrendData("steps", "W", stepsPoints.takeLast(7))
        
        // Extract hourly steps data for daily (D) view
        val stepsHourly = response.time_series.steps_hourly.map { point ->
            AggregatedDataPoint(
                date = point.date.formatDateWithTimezone("HH:mm"),
                total = point.total ?: 0.0,
                avg = point.avg ?: 0.0,
                min = point.min ?: 0.0,
                max = point.max ?: 0.0,
                count = point.count ?: 0,
                unit = "steps"
            )
        }
        if (stepsHourly.isNotEmpty()) {
            cacheTrendData("steps", "D", stepsHourly)
            Log.d(TAG, "Cached ${stepsHourly.size} hourly steps points for D")
        }
        
        _stepsData.value = metricTrendsCache["steps"]?.get("W") ?: stepsPoints

        // ===== CALORIES DATA =====
        val caloriesPoints = response.time_series.calories.map { point ->
            AggregatedDataPoint(
                date = point.date.formatDateWithTimezone("MMM dd"),
                total = point.total ?: 0.0,
                avg = point.avg ?: 0.0,
                min = point.min ?: 0.0,
                max = point.max ?: 0.0,
                count = point.count ?: 0,
                unit = point.unit ?: "kcal"
            )
        }
        Log.d(TAG, "Calories data: ${caloriesPoints.size} daily points")
        
        cacheTrendData("calories", "M", caloriesPoints.takeLast(30))
        cacheTrendData("calories", "W", caloriesPoints.takeLast(7))
        
        // Extract hourly calories data for daily (D) view
        val caloriesHourly = response.time_series.calories_hourly.map { point ->
            AggregatedDataPoint(
                date = point.date.formatDateWithTimezone("HH:mm"),
                total = point.total ?: 0.0,
                avg = point.avg ?: 0.0,
                min = point.min ?: 0.0,
                max = point.max ?: 0.0,
                count = point.count ?: 0,
                unit = "kcal"
            )
        }
        if (caloriesHourly.isNotEmpty()) {
            cacheTrendData("calories", "D", caloriesHourly)
            Log.d(TAG, "Cached ${caloriesHourly.size} hourly calories points for D")
        }
        
        _caloriesData.value = metricTrendsCache["calories"]?.get("W") ?: caloriesPoints

        // ===== HEART RATE DATA =====
        val heartRatePoints = response.time_series.heart_rate.map { point ->
            HeartRateTrendDataPoint(
                date = point.date.formatDateWithTimezone("MMM dd"),
                bpm = point.bpm ?: 0.0,
                min_bpm = point.min_bpm ?: 0.0,
                max_bpm = point.max_bpm ?: 0.0
            )
        }
        Log.d(TAG, "Heart rate data: ${heartRatePoints.size} daily points")
        
        heartRateTrendsCache["M"] = heartRatePoints.takeLast(30)
        heartRateTrendsCache["W"] = heartRatePoints.takeLast(7)
        
        // Extract hourly heart rate data for daily (D) view
        val heartRateHourly = response.time_series.heart_rate_hourly.map { point ->
            HeartRateTrendDataPoint(
                date = point.date.formatDateWithTimezone("HH:mm"),
                bpm = point.bpm ?: 0.0,
                min_bpm = point.min_bpm ?: 0.0,
                max_bpm = point.max_bpm ?: 0.0
            )
        }
        if (heartRateHourly.isNotEmpty()) {
            heartRateTrendsCache["D"] = heartRateHourly
            Log.d(TAG, "Cached ${heartRateHourly.size} hourly heart rate points for D")
        }
        
        _heartRateTrends.value = heartRateTrendsCache["W"] ?: heartRatePoints

        // ===== SLEEP DATA =====
        val sleepTrends = response.time_series.sleep.map { point ->
            SleepTrendDataPoint(
                date = point.date.formatDateWithTimezone("MMM dd"),
                time_in_bed = point.time_in_bed ?: 0.0,
                time_asleep = point.time_asleep ?: 0.0
            )
        }
        Log.d(TAG, "Sleep data: ${sleepTrends.size} daily points")
        
        _sleepTrends.value = sleepTrends
        _dailySleepStages.value = null

        AppDataCache.setComprehensiveMetrics(response)

        Log.d(
            TAG,
            "All cards updated: steps=${_stepsData.value.size}, " +
                "calories=${_caloriesData.value.size}, " +
                "heart_rate=${_heartRateTrends.value.size}, " +
                "sleep=${_sleepTrends.value.size}"
        )
    }
    
    /**
     * Fetch today's summary and devices list
     * Called as part of initialization to populate the top summary cards
     */
    private suspend fun fetchTodaySummaryAndDevices() {
        // Fetch today's summary
        val summaryResult = healthMetricsRepository.getTodaySummary(PATIENT_ID)
        summaryResult.onSuccess { todayResponse ->
            AppDataCache.setTodaySummary(todayResponse)
            AppDataCache.updateLastSyncTime()
            Log.d(TAG, "Today's summary cached")
        }.onFailure { error ->
            Log.e(TAG, "Failed to fetch summary", error)
        }

        // Fetch devices list
        val devicesResult = healthMetricsRepository.getPairedDevices(PATIENT_ID)
        devicesResult.onSuccess { devices ->
            AppDataCache.setDevices(devices)
            AppDataCache.updateLastSyncTime()
            Log.d(TAG, "${devices.size} devices cached")
        }.onFailure { error ->
            Log.e(TAG, "Failed to fetch devices", error)
        }
    }
    // This used to load individual trends. Now replaced by fetchComprehensiveMetrics()
    // which loads all data in a single API call for better performance.
    
    // Removed setDateRange as it caused cross-talk. 
    // Each card now calls its specific update method.

    fun updateSleepTrends(timeframe: String) {
        if (timeframe == "D") {
            loadDailySleepBreakdown()
        } else {
            _dailySleepStages.value = null
            Log.d(TAG, "Using cached sleep trend data for timeframe $timeframe")
        }
    }

    fun updateHeartRateTrends(timeframe: String) {
        heartRateTrendsCache[timeframe]?.let {
            _heartRateTrends.value = it
            return
        }

        if (timeframe == "D") {
            val cachedHourly = AppDataCache.getMetrics("hourly", 1)
            if (cachedHourly != null) {
                val mapped = mapHeartRateAggregated(cachedHourly.metrics["heart_rate"], cachedHourly.period)
                if (mapped.isNotEmpty()) {
                    heartRateTrendsCache["D"] = mapped
                    _heartRateTrends.value = mapped
                    return
                }
            }
            Log.w(TAG, "Hourly heart rate data unavailable in cache; keeping existing values")
            return
        }

        val fallback = when (timeframe) {
            "W" -> heartRateTrendsCache["W"]
            "M" -> heartRateTrendsCache["M"]
            else -> null
        }

        if (!fallback.isNullOrEmpty()) {
            _heartRateTrends.value = fallback
        }
    }

    fun loadStepsTrend(timeframe: String) {
        updateMetricTrendState("steps", timeframe, _stepsData)
    }

    fun loadCaloriesTrend(timeframe: String) {
        updateMetricTrendState("calories", timeframe, _caloriesData)
    }

    private fun getTimeframeParams(timeframe: String): Pair<String, Int> {
        return when (timeframe) {
            "D" -> "hourly" to 1
            "W" -> "daily" to 7
            "M" -> "daily" to 30
            else -> "daily" to 7
        }
    }

    private fun loadHourlyHeartRate() {
        // First, try to use cached comprehensive metrics (heart_rate if available)
        val cachedComprehensiveMetrics = AppDataCache.getComprehensiveMetrics()
        if (cachedComprehensiveMetrics != null && cachedComprehensiveMetrics.time_series.heart_rate.isNotEmpty()) {
            Log.d(TAG, "Using cached heart rate data for hourly view")
            _heartRateTrends.value = cachedComprehensiveMetrics.time_series.heart_rate.map { point ->
                HeartRateTrendDataPoint(
                    date = point.date.formatDateWithTimezone("HH:mm"),
                    bpm = point.bpm ?: 0.0,
                    min_bpm = point.min_bpm ?: 0.0,
                    max_bpm = point.max_bpm ?: 0.0
                )
            }
            return
        }
        
        // If no cached data, fetch from backend
        Log.d(TAG, "No cached heart rate data, fetching from backend")
        viewModelScope.launch {
            // Use getAggregatedMetrics with hourly period
            val result = healthMetricsRepository.getAggregatedMetrics(PATIENT_ID, "hourly", 1)
            result.onSuccess { response ->
                val hourlyData = response.metrics["heart_rate"]?.map { point ->
                    HeartRateTrendDataPoint(
                        date = point.date.formatDateWithTimezone("HH:mm"), // Convert UTC to local time
                        bpm = point.avg,
                        min_bpm = point.min,
                        max_bpm = point.max
                    )
                } ?: emptyList()
                _heartRateTrends.value = hourlyData
            }
        }
    }

    private fun loadDailySleepBreakdown() {
        // Clear trend data so UI swaps to stage view
        _sleepTrends.value = emptyList()

        // 1) Try today's summary cache (fastest, already includes stages + sessions)
        val todaySleepStages = AppDataCache.getTodaySummary()
            ?.summary
            ?.sleep
            ?.stages
        if (todaySleepStages != null) {
            Log.d(TAG, "Using today summary sleep stages for daily view")
            _dailySleepStages.value = todaySleepStages
            return
        }

        val cachedComprehensiveMetrics = AppDataCache.getComprehensiveMetrics()

        // 2) Fallback to comprehensive response summary payload
        val comprehensiveSleepStages = cachedComprehensiveMetrics
            ?.summary
            ?.get("sleep")
            ?.stages
        if (comprehensiveSleepStages != null) {
            Log.d(TAG, "Using comprehensive summary sleep stages for daily view")
            _dailySleepStages.value = comprehensiveSleepStages
            return
        }

        // 3) Last resort: use most recent time-series entry if it contains stages
        val timeSeriesSleep = cachedComprehensiveMetrics
            ?.time_series
            ?.sleep
            ?.firstOrNull { it.stages != null }

        if (timeSeriesSleep?.stages != null) {
            Log.d(TAG, "Using time-series sleep stages for daily view")
            _dailySleepStages.value = timeSeriesSleep.stages
            return
        }

        Log.w(TAG, "No cached sleep stage data available; keeping previous daily breakdown")
    }

    private fun observeCachedWearables() {
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

    // Removed observeWeeklyMetrics and loadWeeklyTrends as they are replaced by specific load functions
    
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
                    saveCacheToDisk()
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
                    saveCacheToDisk()
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
                val success = fetchComprehensiveMetrics(days = 30)
                if (success) {
                    preloadHourlyMetricsIfNeeded()
                    AppDataCache.updateLastSyncTime()
                    Log.d(TAG, "Refresh completed from network")
                } else {
                    Log.d(TAG, "Refresh failed, continuing to show cached data")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during refresh", e)
                Log.d(TAG, "Exception during refresh, will show cached data")
            } finally {
                AppDataCache.setSyncing(false)
            }
        }
    }
    
    fun refresh_OLD_REMOVED() {
        viewModelScope.launch {
            try {
                AppDataCache.setSyncing(true)
                
                val summaryResult = healthMetricsRepository.getTodaySummary(PATIENT_ID)
                summaryResult.onSuccess { todayResponse ->
                    AppDataCache.setTodaySummary(todayResponse)
                    AppDataCache.updateLastSyncTime()
                    Log.d(TAG, "Wearables summary refreshed")
                    saveCacheToDisk()
                }.onFailure { error ->
                    Log.e(TAG, "Refresh summary failed", error)
                    // IMPORTANT: Do NOT show error - cache will be shown with warning indicator
                    Log.d(TAG, "Summary refresh failed, will show cached data")
                }

                val devicesResult = healthMetricsRepository.getPairedDevices(PATIENT_ID)
                devicesResult.onSuccess { devices ->
                    AppDataCache.setDevices(devices)
                    AppDataCache.updateLastSyncTime()
                    saveCacheToDisk()
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
                    saveCacheToDisk()
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
    
    fun unpairDevice(deviceId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Unpairing device: $deviceId")
                val result = healthMetricsRepository.unpairDevice(deviceId)
                
                result.onSuccess {
                    onResult(true, "Device unpaired successfully")
                    // Reload devices after successful unpair
                    val devicesResult = healthMetricsRepository.getPairedDevices(PATIENT_ID)
                    devicesResult.onSuccess { devices ->
                        AppDataCache.setDevices(devices)
                        AppDataCache.updateLastSyncTime()
                        saveCacheToDisk()
                    }.onFailure { error ->
                        Log.e(TAG, "Failed to refresh devices after unpairing", error)
                    }
                }.onFailure { error ->
                    onResult(false, error.message ?: "Failed to unpair device")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }
    
    private fun loadCacheFromDisk() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                
                // Load Summary
                val summaryFile = File(context.filesDir, CACHE_FILE_SUMMARY)
                if (summaryFile.exists()) {
                    val json = summaryFile.readText()
                    val summary = gson.fromJson(json, TodaySummaryResponse::class.java)
                    AppDataCache.setTodaySummary(summary)
                }
                
                // Load Devices
                val devicesFile = File(context.filesDir, CACHE_FILE_DEVICES)
                if (devicesFile.exists()) {
                    val json = devicesFile.readText()
                    val type = object : TypeToken<List<WearableDevice>>() {}.type
                    val devices = gson.fromJson<List<WearableDevice>>(json, type)
                    AppDataCache.setDevices(devices)
                }
                
                // Load Metrics
                val metricsFile = File(context.filesDir, CACHE_FILE_METRICS)
                if (metricsFile.exists()) {
                    val json = metricsFile.readText()
                    val type = object : TypeToken<Map<String, AggregatedMetricsResponse>>() {}.type
                    val metrics = gson.fromJson<Map<String, AggregatedMetricsResponse>>(json, type)
                    metrics.forEach { (key, value) ->
                        val parts = key.split("_")
                        if (parts.size == 2) {
                            AppDataCache.setMetrics(parts[0], parts[1].toIntOrNull() ?: 30, value)
                        }
                    }
                }
                
                // Load Sleep Trends
                val sleepFile = File(context.filesDir, CACHE_FILE_SLEEP)
                if (sleepFile.exists()) {
                    val json = sleepFile.readText()
                    val type = object : TypeToken<List<SleepTrendDataPoint>>() {}.type
                    val sleepData = gson.fromJson<List<SleepTrendDataPoint>>(json, type)
                    _sleepTrends.value = sleepData
                }
                
                // Load Heart Rate Trends
                val heartFile = File(context.filesDir, CACHE_FILE_HEART)
                if (heartFile.exists()) {
                    val json = heartFile.readText()
                    val type = object : TypeToken<List<HeartRateTrendDataPoint>>() {}.type
                    val heartData = gson.fromJson<List<HeartRateTrendDataPoint>>(json, type)
                    _heartRateTrends.value = heartData
                }
                
                Log.d(TAG, "Cache loaded from disk")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading cache from disk", e)
            }
        }
    }

    private fun saveCacheToDisk() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                
                // Save Summary
                AppDataCache.getTodaySummary()?.let { summary ->
                    File(context.filesDir, CACHE_FILE_SUMMARY).writeText(gson.toJson(summary))
                }
                
                // Save Devices
                val devices = AppDataCache.getDevices()
                if (devices.isNotEmpty()) {
                    File(context.filesDir, CACHE_FILE_DEVICES).writeText(gson.toJson(devices))
                }
                
                // Save Metrics (We can't easily get the whole map from AppDataCache as it exposes StateFlow of map)
                // But we can access the flow value
                val metrics = AppDataCache.metricsCache.value
                if (metrics.isNotEmpty()) {
                    File(context.filesDir, CACHE_FILE_METRICS).writeText(gson.toJson(metrics))
                }
                
                // Save Sleep Trends
                val sleepData = _sleepTrends.value
                if (sleepData.isNotEmpty()) {
                    File(context.filesDir, CACHE_FILE_SLEEP).writeText(gson.toJson(sleepData))
                }
                
                // Save Heart Rate Trends
                val heartData = _heartRateTrends.value
                if (heartData.isNotEmpty()) {
                    File(context.filesDir, CACHE_FILE_HEART).writeText(gson.toJson(heartData))
                }
                
                Log.d(TAG, "Cache saved to disk")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving cache to disk", e)
            }
        }
    }

    private fun cacheTrendData(metric: String, timeframe: String, data: List<AggregatedDataPoint>) {
        if (data.isEmpty()) return
        val metricCache = metricTrendsCache.getOrPut(metric) { mutableMapOf() }
        metricCache[timeframe] = data
    }

    private suspend fun preloadHourlyMetricsIfNeeded() {
        // Always extract hourly data from fresh comprehensive response
        Log.d(TAG, "Extracting hourly metrics from comprehensive response")
        
        // Hourly data is now processed directly in processComprehensiveResponse()
        Log.d(TAG, "Hourly metrics already extracted during comprehensive response processing")
    }

    private fun mapAggregatedPoints(
        points: List<AggregatedDataPoint>?,
        period: String
    ): List<AggregatedDataPoint> {
        if (points.isNullOrEmpty()) {
            Log.w(TAG, "mapAggregatedPoints received null or empty points for period $period")
            return emptyList()
        }
        val dateFormat = if (period == "hourly") "HH:mm" else "MMM dd"
        val mapped = points.map { point ->
            point.copy(date = point.date.formatDateWithTimezone(dateFormat))
        }
        Log.d(TAG, "Mapped ${mapped.size} aggregated points for period $period, sample: ${mapped.take(2)}")
        return mapped
    }

    private fun mapHeartRateAggregated(
        points: List<AggregatedDataPoint>?,
        period: String
    ): List<HeartRateTrendDataPoint> {
        if (points.isNullOrEmpty()) {
            Log.w(TAG, "mapHeartRateAggregated received null or empty points for period $period")
            return emptyList()
        }
        val dateFormat = if (period == "hourly") "HH:mm" else "MMM dd"
        val mapped = points.map { point ->
            HeartRateTrendDataPoint(
                date = point.date.formatDateWithTimezone(dateFormat),
                bpm = point.avg,
                min_bpm = point.min,
                max_bpm = point.max
            )
        }
        Log.d(TAG, "Mapped ${mapped.size} heart rate points for period $period, sample: ${mapped.take(2)}")
        return mapped
    }

    private fun cacheAggregatedResponse(
        response: AggregatedMetricsResponse,
        timeframe: String
    ) {
        response.metrics["steps"]?.let { points ->
            val mapped = mapAggregatedPoints(points, response.period)
            cacheTrendData("steps", timeframe, mapped)
            if (timeframe == "D" && _stepsData.value.isEmpty()) {
                _stepsData.value = mapped
            }
        }

        response.metrics["calories"]?.let { points ->
            val mapped = mapAggregatedPoints(points, response.period)
            cacheTrendData("calories", timeframe, mapped)
            if (timeframe == "D" && _caloriesData.value.isEmpty()) {
                _caloriesData.value = mapped
            }
        }

        response.metrics["heart_rate"]?.let { points ->
            val mapped = mapHeartRateAggregated(points, response.period)
            heartRateTrendsCache[timeframe] = mapped
            if (timeframe == "D" && mapped.isNotEmpty()) {
                _heartRateTrends.value = mapped
            }
        }
    }

    private fun updateMetricTrendState(
        metric: String,
        timeframe: String,
        target: MutableStateFlow<List<AggregatedDataPoint>>
    ) {
        val cached = metricTrendsCache[metric]?.get(timeframe)
        if (cached != null && cached.isNotEmpty()) {
            target.value = cached
            return
        }

        val (period, days) = getTimeframeParams(timeframe)
        val cachedMetrics = AppDataCache.getMetrics(period, days)
        if (cachedMetrics != null) {
            val mapped = mapAggregatedPoints(cachedMetrics.metrics[metric], period)
            cacheTrendData(metric, timeframe, mapped)
            target.value = mapped
            return
        }

        Log.w(TAG, "No cached $metric data for timeframe $timeframe; keeping previous values")
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


