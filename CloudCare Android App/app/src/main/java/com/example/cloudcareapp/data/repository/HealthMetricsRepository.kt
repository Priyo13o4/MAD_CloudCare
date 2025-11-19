package com.example.cloudcareapp.data.repository

import android.util.Log
import com.example.cloudcareapp.data.model.*
import com.example.cloudcareapp.data.remote.CloudCareApiService
import com.example.cloudcareapp.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for health metrics data
 * 
 * Handles all network communication with CloudCare backend.
 * Uses Result<T> pattern for consistent error handling.
 * 
 * Usage:
 * ```kotlin
 * val repository = HealthMetricsRepository()
 * val result = repository.getTodaySummary(patientId)
 * 
 * result.onSuccess { summary ->
 *     // Use the data
 * }.onFailure { error ->
 *     // Handle error
 * }
 * ```
 */
class HealthMetricsRepository(
    private val apiService: CloudCareApiService = RetrofitClient.apiService
) {
    
    companion object {
        private const val TAG = "HealthMetricsRepository"
    }
    
    /**
     * Get recent health metrics for the last N hours
     * 
     * @param patientId Patient's unique ID
     * @param hours Number of hours to look back (default: 24)
     * @return Result containing list of recent metrics or error
     */
    suspend fun getRecentMetrics(
        patientId: String,
        hours: Int = 24
    ): Result<RecentMetricsResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching recent metrics for patient: $patientId (last $hours hours)")
            val response = apiService.getRecentMetrics(patientId, hours)
            Log.d(TAG, "Successfully fetched ${response.count} recent metrics")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch recent metrics", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get today's aggregated health summary
     * 
     * @param patientId Patient's unique ID
     * @return Result containing today's summary or error
     */
    suspend fun getTodaySummary(
        patientId: String
    ): Result<TodaySummaryResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching today's summary for patient: $patientId")
            val response = apiService.getTodaySummary(patientId)
            Log.d(TAG, "Successfully fetched today's summary for ${response.date}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch today's summary", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get aggregated metrics by time period
     * 
     * @param patientId Patient's unique ID
     * @param period Aggregation period: "hourly", "daily", or "weekly"
     * @param days Number of days to look back
     * @return Result containing aggregated metrics or error
     */
    suspend fun getAggregatedMetrics(
        patientId: String,
        period: String = "daily",
        days: Int = 30
    ): Result<AggregatedMetricsResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching aggregated metrics: period=$period, days=$days")
            val response = apiService.getAggregatedMetrics(patientId, period, days)
            Log.d(TAG, "Successfully fetched aggregated metrics")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch aggregated metrics", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get accurate sleep trends with time_in_bed vs time_asleep separation.
     * 
     * This uses the dedicated sleep-trends endpoint that correctly handles
     * Apple Health sleep data structure by separating 'inBed' samples from
     * actual sleep stages ('core', 'deep', 'rem').
     * 
     * @param patientId Patient's unique ID
     * @param days Number of days to look back (default: 30)
     * @return Result containing daily sleep trends or error
     */
    suspend fun getSleepTrends(
        patientId: String,
        days: Int = 30
    ): Result<SleepTrendsResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching sleep trends: days=$days")
            val response = apiService.getSleepTrends(patientId, days)
            Log.d(TAG, "Successfully fetched ${response.data.size} sleep trend data points")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch sleep trends", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get heart rate trends with baseline positioning at 72 BPM.
     * 
     * This uses the dedicated heart-rate-trends endpoint that returns
     * daily average/min/max BPM for suspended bar visualization where
     * bars extend both above and below the 72 BPM baseline.
     * 
     * @param patientId Patient's unique ID
     * @param days Number of days to look back (default: 30)
     * @return Result containing daily heart rate trends or error
     */
    suspend fun getHeartRateTrends(
        patientId: String,
        days: Int = 30
    ): Result<HeartRateTrendsResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching heart rate trends: days=$days")
            val response = apiService.getHeartRateTrends(patientId, days)
            Log.d(TAG, "Successfully fetched ${response.data.size} heart rate trend data points")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch heart rate trends", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get specific metric type over date range
     * 
     * @param patientId Patient's unique ID
     * @param metricType Type of metric (e.g., "heart_rate", "steps")
     * @param startDate Optional start date (ISO format)
     * @param endDate Optional end date (ISO format)
     * @return Result containing time-series metrics or error
     */
    suspend fun getMetricsByType(
        patientId: String,
        metricType: String,
        startDate: String? = null,
        endDate: String? = null
    ): Result<MetricsByTypeResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching metrics by type: $metricType")
            val response = apiService.getMetricsByType(patientId, metricType, startDate, endDate)
            Log.d(TAG, "Successfully fetched ${response.count} metrics of type $metricType")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch metrics by type: $metricType", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all paired iOS devices for an Android user
     * 
     * @param androidUserId Android user's unique ID
     * @return Result containing list of paired devices or error
     */
    suspend fun getPairedDevices(
        androidUserId: String
    ): Result<List<WearableDevice>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching paired devices for user: $androidUserId")
            val devices = apiService.getPairedDevices(androidUserId)
            Log.d(TAG, "Successfully fetched ${devices.size} paired device(s)")
            Result.success(devices)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch paired devices", e)
            Result.failure(e)
        }
    }
    
    /**
     * Pair a new wearable device
     * 
     * @param request Pairing request with device details
     * @return Result containing pairing response or error
     */
    suspend fun pairDevice(
        request: PairingRequest
    ): Result<PairingResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Pairing device: ${request.deviceName} (${request.deviceType})")
            val response = apiService.pairDevice(request)
            Log.d(TAG, "Successfully paired device: ${response.message}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pair device", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if the backend is reachable
     * 
     * @param patientId Patient ID to test with
     * @return true if backend responds successfully, false otherwise
     */
    suspend fun checkConnection(patientId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            apiService.getTodaySummary(patientId)
            true
        } catch (e: Exception) {
            Log.w(TAG, "Backend connection check failed", e)
            false
        }
    }
}
