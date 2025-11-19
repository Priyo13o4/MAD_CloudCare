package com.example.cloudcareapp.data.remote

import com.example.cloudcareapp.data.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * CloudCare Backend API Service
 * 
 * Base URL: https://cloudcare.pipfactor.com/api/v1/
 * All endpoints are currently public for testing
 */
interface CloudCareApiService {
    
    /**
     * Get recent health metrics for the last N hours
     * 
     * @param patientId Patient's unique ID
     * @param hours Number of hours to look back (default: 24)
     * @return List of recent health metrics
     */
    @GET("wearables/metrics/recent")
    suspend fun getRecentMetrics(
        @Query("patient_id") patientId: String,
        @Query("hours") hours: Int = 24
    ): RecentMetricsResponse
    
    /**
     * Get today's health summary with comparison to yesterday
     * 
     * @param patientId Patient's unique ID
     * @return Aggregated summary for today
     */
    @GET("wearables/summary/today")
    suspend fun getTodaySummary(
        @Query("patient_id") patientId: String
    ): TodaySummaryResponse
    
    /**
     * Get aggregated metrics by time period
     * 
     * @param patientId Patient's unique ID
     * @param period Aggregation period: "hourly", "daily", or "weekly"
     * @param days Number of days to look back
     * @return Aggregated metrics grouped by type and time
     */
    @GET("wearables/metrics/aggregated")
    suspend fun getAggregatedMetrics(
        @Query("patient_id") patientId: String,
        @Query("period") period: String = "daily",
        @Query("days") days: Int = 30
    ): AggregatedMetricsResponse
    
    /**
     * Get accurate sleep trends with time_in_bed vs time_asleep separation.
     * 
     * This endpoint correctly handles Apple Health sleep data structure:
     * - time_in_bed: From 'inBed' samples
     * - time_asleep: Sum of 'core' + 'deep' + 'rem' (excludes 'awake')
     * 
     * @param patientId Patient's unique ID
     * @param days Number of days to look back (default: 30)
     * @return Daily sleep trends with honest data
     */
    @GET("wearables/metrics/sleep-trends")
    suspend fun getSleepTrends(
        @Query("patient_id") patientId: String,
        @Query("days") days: Int = 30
    ): SleepTrendsResponse
    
    /**
     * Get heart rate trends with baseline positioning.
     * 
     * Heart rate baseline is 72 BPM (Apple Health standard).
     * Data points can be above or below baseline for suspended bar visualization.
     * 
     * @param patientId Patient's unique ID
     * @param days Number of days to look back (default: 30)
     * @return Daily heart rate trends with min/max/average BPM
     */
    @GET("wearables/metrics/heart-rate-trends")
    suspend fun getHeartRateTrends(
        @Query("patient_id") patientId: String,
        @Query("days") days: Int = 30
    ): HeartRateTrendsResponse
    
    /**
     * Get specific metric type over date range
     * 
     * @param patientId Patient's unique ID
     * @param metricType Type of metric (e.g., "heart_rate", "steps", "calories")
     * @param startDate Optional start date (ISO format)
     * @param endDate Optional end date (ISO format)
     * @return Time-series array of individual metrics
     */
    @GET("wearables/metrics/by-type")
    suspend fun getMetricsByType(
        @Query("patient_id") patientId: String,
        @Query("metric_type") metricType: String,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("limit") limit: Int? = null
    ): MetricsByTypeResponse
    
    @GET("wearables/devices")
    suspend fun getDevices(
        @Query("patient_id") patientId: String
    ): List<WearableDevice>
    
    @GET("wearables/devices/paired")
    suspend fun getPairedDevices(
        @Query("android_user_id") androidUserId: String
    ): List<WearableDevice>
    
    /**
     * Pair a new wearable device
     * 
     * @param request Pairing request with device details
     * @return Pairing response with pairing ID and status
     */
    @POST("wearables/devices/pair")
    suspend fun pairDevice(
        @Body request: PairingRequest
    ): PairingResponse
}
