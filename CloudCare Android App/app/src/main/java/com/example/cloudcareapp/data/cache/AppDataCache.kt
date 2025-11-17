package com.example.cloudcareapp.data.cache

import android.util.Log
import com.example.cloudcareapp.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Singleton cache for app-wide health data and device information.
 * Prevents redundant API calls when navigating between screens.
 * 
 * Stores:
 * - Today's health summary
 * - Wearable devices list
 * - Aggregated metrics for different date ranges
 * - Last sync timestamp
 * 
 * Thread-safe using MutableStateFlow for reactive updates.
 */
object AppDataCache {
    private const val TAG = "AppDataCache"
    
    // Cache for today's summary
    private val _todaySummaryCache = MutableStateFlow<TodaySummaryResponse?>(null)
    val todaySummaryCache: StateFlow<TodaySummaryResponse?> = _todaySummaryCache.asStateFlow()
    
    // Cache for wearable devices
    private val _devicesCache = MutableStateFlow<List<WearableDevice>>(emptyList())
    val devicesCache: StateFlow<List<WearableDevice>> = _devicesCache.asStateFlow()
    
    // Cache for aggregated metrics (keyed by "period_days" e.g., "daily_7")
    private val _metricsCache = mutableMapOf<String, AggregatedMetricsResponse>()
    private val _metricsCacheFlow = MutableStateFlow<Map<String, AggregatedMetricsResponse>>(emptyMap())
    val metricsCache: StateFlow<Map<String, AggregatedMetricsResponse>> = _metricsCacheFlow.asStateFlow()
    
    // Last sync timestamps (for each endpoint)
    private val _lastSyncTimes = mutableMapOf<String, LocalDateTime>()
    private val _lastSyncTimeFlow = MutableStateFlow<LocalDateTime?>(null)
    val lastSyncTime: StateFlow<LocalDateTime?> = _lastSyncTimeFlow.asStateFlow()
    
    // Is currently syncing
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    /**
     * Check if cache has valid data for today's summary
     */
    fun hasTodaySummary(): Boolean {
        return _todaySummaryCache.value != null
    }
    
    /**
     * Check if cache has valid data for devices
     */
    fun hasDevices(): Boolean {
        return _devicesCache.value.isNotEmpty()
    }
    
    /**
     * Check if cache has valid data for aggregated metrics
     */
    fun hasMetrics(period: String, days: Int): Boolean {
        val cacheKey = "${period}_${days}"
        return _metricsCache.containsKey(cacheKey)
    }
    
    /**
     * Get cached today's summary
     */
    fun getTodaySummary(): TodaySummaryResponse? {
        return _todaySummaryCache.value
    }
    
    /**
     * Set today's summary cache
     */
    fun setTodaySummary(response: TodaySummaryResponse) {
        Log.d(TAG, "Caching today's summary")
        _todaySummaryCache.value = response
        _lastSyncTimes["summary"] = LocalDateTime.now()
    }
    
    /**
     * Get cached devices
     */
    fun getDevices(): List<WearableDevice> {
        return _devicesCache.value
    }
    
    /**
     * Set devices cache
     */
    fun setDevices(devices: List<WearableDevice>) {
        Log.d(TAG, "Caching ${devices.size} devices")
        _devicesCache.value = devices
        _lastSyncTimes["devices"] = LocalDateTime.now()
    }
    
    /**
     * Get cached metrics for specific period
     */
    fun getMetrics(period: String, days: Int): AggregatedMetricsResponse? {
        val cacheKey = "${period}_${days}"
        return _metricsCache[cacheKey]
    }
    
    /**
     * Set metrics cache for specific period
     */
    fun setMetrics(period: String, days: Int, response: AggregatedMetricsResponse) {
        val cacheKey = "${period}_${days}"
        Log.d(TAG, "Caching metrics for $cacheKey")
        _metricsCache[cacheKey] = response
        _metricsCacheFlow.value = HashMap(_metricsCache)
        _lastSyncTimes[cacheKey] = LocalDateTime.now()
    }
    
    /**
     * Clear all cached data
     */
    fun clearAll() {
        Log.d(TAG, "Clearing all cache")
        _todaySummaryCache.value = null
        _devicesCache.value = emptyList()
        _metricsCache.clear()
        _metricsCacheFlow.value = emptyMap()
        _lastSyncTimes.clear()
        _lastSyncTimeFlow.value = null
    }
    
    /**
     * Set sync state
     */
    fun setSyncing(syncing: Boolean) {
        _isSyncing.value = syncing
    }
    
    /**
     * Update overall last sync time and format it for display
     */
    fun updateLastSyncTime() {
        val now = LocalDateTime.now()
        _lastSyncTimeFlow.value = now
        Log.d(TAG, "Updated last sync time: $now")
    }
    
    /**
     * Get formatted last sync time (e.g., "2 mins ago", "Just now")
     * Always returns a non-null, non-empty string
     */
    fun getFormattedLastSyncTime(): String {
        return try {
            val lastSync = _lastSyncTimeFlow.value ?: return "Never"
            val now = LocalDateTime.now()
            
            val minutesDiff = java.time.temporal.ChronoUnit.MINUTES.between(lastSync, now)
            
            when {
                minutesDiff == 0L -> "Just now"
                minutesDiff == 1L -> "1 min ago"
                minutesDiff < 60 -> "$minutesDiff mins ago"
                minutesDiff < 1440 -> {
                    val hoursDiff = minutesDiff / 60
                    if (hoursDiff == 1L) "1 hour ago" else "$hoursDiff hours ago"
                }
                else -> {
                    val daysDiff = minutesDiff / 1440
                    if (daysDiff == 1L) "1 day ago" else "$daysDiff days ago"
                }
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * Get last sync time as LocalDateTime
     */
    fun getLastSyncDateTime(): LocalDateTime? {
        return _lastSyncTimeFlow.value
    }
}
