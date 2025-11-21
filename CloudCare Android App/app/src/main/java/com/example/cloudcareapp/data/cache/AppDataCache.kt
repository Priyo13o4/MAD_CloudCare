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
    
    // Cache for comprehensive metrics (single API call with all data)
    private val _comprehensiveMetricsCache = MutableStateFlow<ComprehensiveMetricsResponse?>(null)
    val comprehensiveMetricsCache: StateFlow<ComprehensiveMetricsResponse?> = _comprehensiveMetricsCache.asStateFlow()
    
    // Last sync timestamps (for each endpoint)
    private val _lastSyncTimes = mutableMapOf<String, LocalDateTime>()
    private val _lastSyncTimeFlow = MutableStateFlow<LocalDateTime?>(null)
    val lastSyncTime: StateFlow<LocalDateTime?> = _lastSyncTimeFlow.asStateFlow()

    // Global syncing state shared across screens (true while refresh in progress)
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    // Patient info cache
    private var cachedPatientId: String? = null
    private var cachedPatientName: String? = null
    
    // Doctor info cache
    private var cachedDoctorId: String? = null
    
    // Hospital info cache
    private var cachedHospitalId: String? = null
    
    // User name cache (for all roles)
    private var cachedUserName: String? = null
    
    // Hospital Cache
    private val _hospitalDashboardCache = MutableStateFlow<HospitalDashboardStats?>(null)
    val hospitalDashboardCache: StateFlow<HospitalDashboardStats?> = _hospitalDashboardCache.asStateFlow()

    private val _hospitalPatientsCache = MutableStateFlow<List<PatientSummary>>(emptyList())
    val hospitalPatientsCache: StateFlow<List<PatientSummary>> = _hospitalPatientsCache.asStateFlow()

    private val _hospitalDoctorsCache = MutableStateFlow<List<DoctorSummary>>(emptyList())
    val hospitalDoctorsCache: StateFlow<List<DoctorSummary>> = _hospitalDoctorsCache.asStateFlow()

    private val _hospitalProfileCache = MutableStateFlow<HospitalProfileResponse?>(null)
    val hospitalProfileCache: StateFlow<HospitalProfileResponse?> = _hospitalProfileCache.asStateFlow()

    // Doctor Profile Cache
    private val _doctorProfileCache = MutableStateFlow<DoctorProfileResponse?>(null)
    val doctorProfileCache: StateFlow<DoctorProfileResponse?> = _doctorProfileCache.asStateFlow()

    /**
     * Get cached patient ID
     */
    fun getPatientId(): String? {
        return cachedPatientId
    }
    
    /**
     * Set patient ID cache
     */
    fun setPatientId(patientId: String) {
        this.cachedPatientId = patientId
    }
    
    /**
     * Get cached patient name
     */
    fun getPatientName(): String? {
        return cachedPatientName
    }
    
    /**
     * Set patient name cache
     */
    fun setPatientName(patientName: String) {
        this.cachedPatientName = patientName
    }
    
    /**
     * Get cached doctor ID
     */
    fun getDoctorId(): String? {
        return cachedDoctorId
    }
    
    /**
     * Set doctor ID cache
     */
    fun setDoctorId(doctorId: String) {
        this.cachedDoctorId = doctorId
    }
    
    /**
     * Get cached hospital ID
     */
    fun getHospitalId(): String? {
        return cachedHospitalId
    }
    
    /**
     * Set hospital ID cache
     */
    fun setHospitalId(hospitalId: String) {
        this.cachedHospitalId = hospitalId
    }
    
    /**
     * Get cached user name (for all roles)
     */
    fun getUserName(): String? {
        return cachedUserName
    }
    
    /**
     * Set user name cache (for all roles)
     */
    fun setUserName(userName: String) {
        this.cachedUserName = userName
    }
    
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
     * Get cached comprehensive metrics (all data in one call)
     */
    fun getComprehensiveMetrics(): ComprehensiveMetricsResponse? {
        return _comprehensiveMetricsCache.value
    }
    
    /**
     * Set comprehensive metrics cache
     */
    fun setComprehensiveMetrics(response: ComprehensiveMetricsResponse) {
        Log.d(TAG, "Caching comprehensive metrics")
        _comprehensiveMetricsCache.value = response
        _lastSyncTimes["comprehensive"] = LocalDateTime.now()
    }
    
    /**
     * Check if cache has comprehensive metrics
     */
    fun hasComprehensiveMetrics(): Boolean {
        return _comprehensiveMetricsCache.value != null
    }
    
    /**
     * Get cached hospital dashboard stats
     */
    fun getHospitalDashboardStats(): HospitalDashboardStats? {
        return _hospitalDashboardCache.value
    }

    /**
     * Set hospital dashboard stats cache
     */
    fun setHospitalDashboardStats(stats: HospitalDashboardStats) {
        Log.d(TAG, "Caching hospital dashboard stats")
        _hospitalDashboardCache.value = stats
        _lastSyncTimes["hospital_dashboard"] = LocalDateTime.now()
    }

    /**
     * Get cached hospital patients
     */
    fun getHospitalPatients(): List<PatientSummary> {
        return _hospitalPatientsCache.value
    }

    /**
     * Set hospital patients cache
     */
    fun setHospitalPatients(patients: List<PatientSummary>) {
        Log.d(TAG, "Caching ${patients.size} hospital patients")
        _hospitalPatientsCache.value = patients
        _lastSyncTimes["hospital_patients"] = LocalDateTime.now()
    }

    /**
     * Get cached hospital doctors
     */
    fun getHospitalDoctors(): List<DoctorSummary> {
        return _hospitalDoctorsCache.value
    }

    /**
     * Set hospital doctors cache
     */
    fun setHospitalDoctors(doctors: List<DoctorSummary>) {
        Log.d(TAG, "Caching ${doctors.size} hospital doctors")
        _hospitalDoctorsCache.value = doctors
        _lastSyncTimes["hospital_doctors"] = LocalDateTime.now()
    }

    /**
     * Get cached hospital profile
     */
    fun getHospitalProfile(): HospitalProfileResponse? {
        return _hospitalProfileCache.value
    }

    /**
     * Set hospital profile cache
     */
    fun setHospitalProfile(profile: HospitalProfileResponse) {
        Log.d(TAG, "Caching hospital profile")
        _hospitalProfileCache.value = profile
        _lastSyncTimes["hospital_profile"] = LocalDateTime.now()
    }

    /**
     * Get cached doctor profile
     */
    fun getDoctorProfile(): DoctorProfileResponse? {
        return _doctorProfileCache.value
    }

    /**
     * Set doctor profile cache
     */
    fun setDoctorProfile(profile: DoctorProfileResponse) {
        Log.d(TAG, "Caching doctor profile")
        _doctorProfileCache.value = profile
        _lastSyncTimes["doctor_profile"] = LocalDateTime.now()
    }

    /**
     * Check if hospital cache has valid data
     */
    fun hasHospitalData(): Boolean {
        return _hospitalDashboardCache.value != null
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
        _comprehensiveMetricsCache.value = null
        
        // Clear hospital cache
        _hospitalDashboardCache.value = null
        _hospitalPatientsCache.value = emptyList()
        _hospitalDoctorsCache.value = emptyList()
        _hospitalProfileCache.value = null
        _doctorProfileCache.value = null
        
        // Clear user IDs
        cachedPatientId = null
        cachedDoctorId = null
        cachedHospitalId = null
        cachedPatientName = null
        cachedUserName = null
        
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
