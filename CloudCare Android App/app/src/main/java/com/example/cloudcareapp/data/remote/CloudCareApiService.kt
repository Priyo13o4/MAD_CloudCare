package com.example.cloudcareapp.data.remote

import com.example.cloudcareapp.data.model.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * CloudCare Backend API Service
 * 
 * Base URL: https://cloudcare.pipfactor.com/api/v1/
 * All endpoints are currently public for testing
 */
interface CloudCareApiService {
    
    /**
     * ========================================
     * 🚀 COMPREHENSIVE METRICS ENDPOINT
     * ========================================
     * 
     * Get ALL health data in a single API call:
     * - Today's summary for all metrics (steps, calories, heart rate, sleep, etc.)
     * - Time-series data for charts (last 30 days)
     * - Device sync information
     * 
     * This REPLACES multiple individual API calls and fixes card synchronization bugs.
     * Use this for the main dashboard/wearables screen.
     * 
     * @param patientId Patient's unique ID
     * @param days Number of days to fetch time-series data (default: 30)
     * @return Everything you need in one response!
     */
    @GET("wearables/metrics/comprehensive")
    suspend fun getComprehensiveMetrics(
        @Query("patient_id") patientId: String,
        @Query("days") days: Int = 30
    ): ComprehensiveMetricsResponse
    
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
    
    /**
     * Unpair a wearable device
     * 
     * @param pairingId The pairing ID to unpair
     * @return Success response (HTTP 204 No Content)
     */
    @DELETE("wearables/devices/unpair/{pairing_id}")
    suspend fun unpairDevice(
        @Path("pairing_id") pairingId: String
    ): Unit
    
    /**
     * ========================================
     * 📱 QR CODE - PATIENT HEALTH RECORD
     * ========================================
     * 
     * Get complete patient health data by patient ID
     * Used when scanning patient QR codes
     * 
     * @param patientId Patient's unique ID from QR code
     * @return Complete patient data including wearables, records, devices
     */
    @GET("patients/{patient_id}/complete")
    suspend fun getPatientCompleteData(
        @Path("patient_id") patientId: String
    ): PatientHealthRecordResponse
    
    /**
     * ========================================
     * 📄 DOCUMENT UPLOAD & MANAGEMENT
     * ========================================
     */
    
    /**
     * Upload medical document/record
     * 
     * @param request Upload request with file and metadata
     * @return Upload response with document ID and URL
     */
    @POST("patients/documents/upload")
    suspend fun uploadDocument(
        @Body request: DocumentUploadRequest
    ): DocumentUploadResponse
    
    /**
     * Get patient's documents
     * 
     * @param patientId Patient's unique ID
     * @return List of patient documents
     */
    @GET("patients/{patient_id}/documents")
    suspend fun getPatientDocuments(
        @Path("patient_id") patientId: String
    ): DocumentsResponse
    
    /**
     * Delete a document
     * 
     * @param documentId Document ID to delete
     */
    @DELETE("patients/documents/{document_id}")
    suspend fun deleteDocument(
        @Path("document_id") documentId: String
    ): Unit
    
    /**
     * ========================================
     * 👨‍⚕️ DOCTOR PROFILE & DATA
     * ========================================
     */
    
    /**
     * Get doctor profile
     * 
     * @param doctorId Doctor's unique ID
     * @return Doctor profile data
     */
    @GET("doctors/{doctor_id}/profile")
    suspend fun getDoctorProfile(
        @Path("doctor_id") doctorId: String
    ): DoctorProfileResponse
    
    /**
     * Update doctor profile
     * 
     * @param doctorId Doctor's unique ID
     * @param request Updated profile data
     */
    @POST("doctors/{doctor_id}/profile")
    suspend fun updateDoctorProfile(
        @Path("doctor_id") doctorId: String,
        @Body request: UpdateDoctorProfileRequest
    ): DoctorProfileResponse
    
    /**
     * Get doctor notifications
     * 
     * @param doctorId Doctor's unique ID
     * @return List of notifications
     */
    @GET("doctors/{doctor_id}/notifications")
    suspend fun getDoctorNotifications(
        @Path("doctor_id") doctorId: String
    ): NotificationsResponse
    
    /**
     * ========================================
     * 👤 PATIENT PROFILE & DATA
     * ========================================
     */
    
    /**
     * Get patient profile
     * 
     * @param patientId Patient's unique ID
     * @return Patient profile data
     */
    @GET("patients/{patient_id}/profile")
    suspend fun getPatientProfile(
        @Path("patient_id") patientId: String
    ): PatientProfileResponse
    
    /**
     * Update patient profile
     * 
     * @param patientId Patient's unique ID
     * @param request Updated profile data
     */
    @POST("patients/{patient_id}/profile")
    suspend fun updatePatientProfile(
        @Path("patient_id") patientId: String,
        @Body request: UpdatePatientProfileRequest
    ): PatientProfileResponse
}
