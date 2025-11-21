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
    @POST("documents/upload")
    suspend fun uploadDocument(
        @Body request: CreateMedicalRecordRequest
    ): MedicalRecordResponse
    
    /**
     * Get patient's documents
     * 
     * @param patientId Patient's unique ID
     * @return List of patient documents
     */
    @GET("documents/{patient_id}")
    suspend fun getPatientDocuments(
        @Path("patient_id") patientId: String
    ): List<MedicalRecordResponse>
    
    /**
     * Delete a document
     * 
     * @param documentId Document ID to delete
     */
    @DELETE("documents/{document_id}")
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
     * Get doctor's assigned patients
     * 
     * @param doctorId Doctor's unique ID
     * @return List of assigned patients
     */
    @GET("doctors/{doctor_id}/patients")
    suspend fun getDoctorPatients(
        @Path("doctor_id") doctorId: String
    ): List<DoctorPatientResponse>
    
    /**
     * ========================================
     * 🏥 HOSPITAL PROFILE
     * ========================================
     */
    
    /**
     * Get hospital profile
     * 
     * @param hospitalId Hospital's unique ID
     * @return Hospital profile data
     */
    @GET("hospitals/{hospital_id}/profile")
    suspend fun getHospitalProfile(
        @Path("hospital_id") hospitalId: String
    ): HospitalProfileResponse

    @GET("hospitals/")
    suspend fun getHospitals(): List<HospitalProfileResponse>

    @GET("hospitals/search")
    suspend fun searchHospitals(
        @Query("query") query: String? = null
    ): List<HospitalProfileResponse>

    @GET("hospitals/{hospital_id}/dashboard")
    suspend fun getHospitalDashboardStats(
        @Path("hospital_id") hospitalId: String
    ): HospitalDashboardStats

    @GET("hospitals/{hospital_id}/doctors")
    suspend fun getHospitalDoctors(
        @Path("hospital_id") hospitalId: String
    ): List<DoctorSummary>

    @GET("hospitals/{hospital_id}/patients")
    suspend fun getHospitalPatients(
        @Path("hospital_id") hospitalId: String,
        @Query("status_filter") statusFilter: String? = null
    ): List<PatientSummary>

    @retrofit2.http.PUT("hospitals/{hospital_id}/resources")
    suspend fun updateHospitalResources(
        @Path("hospital_id") hospitalId: String,
        @Body resources: ResourceUpdate
    ): ResourceUpdateResponse

    @POST("hospitals/{hospital_id}/patients/{patient_id}/discharge")
    suspend fun dischargePatient(
        @Path("hospital_id") hospitalId: String,
        @Path("patient_id") patientId: String,
        @Body request: DischargePatientRequest
    ): GenericResponse

    @POST("patients/lookup-records")
    suspend fun lookupPatientRecords(
        @Body request: RecordLookupRequest
    ): List<MedicalRecordSummary>
    
    /**
     * ========================================
     * 👤 PATIENT PROFILE & DATA
     * ========================================
     * ========================================
     * 🔐 CONSENT MANAGEMENT
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
     * Create consent request when doctor scans patient QR code
     * 
     * @param request Consent request data with patient_id, doctor_id, facility_name
     * @return Created consent request
     */
    @POST("consents/request")
    suspend fun createConsentRequest(
        @Body request: CreateConsentRequest
    ): ConsentResponse
    
    /**
     * Get all consent requests for a patient
     * 
     * @param patientId Patient's unique ID
     * @param statusFilter Optional filter: PENDING, APPROVED, DENIED
     * @return List of consent requests
     */
    @GET("consents/patient/{patient_id}")
    suspend fun getPatientConsents(
        @Path("patient_id") patientId: String,
        @Query("status_filter") statusFilter: String? = null
    ): List<ConsentResponse>
    
    /**
     * Update consent request status (approve/deny)
     * 
     * @param consentId Consent request ID
     * @param request Status update (APPROVED or DENIED)
     * @return Updated consent request
     */
    @retrofit2.http.PATCH("consents/{consent_id}")
    suspend fun updateConsentStatus(
        @Path("consent_id") consentId: String,
        @Body request: UpdateConsentRequest
    ): ConsentResponse
    
    /**
     * Delete consent request
     * 
     * @param consentId Consent request ID
     */
    @DELETE("consents/{consent_id}")
    suspend fun deleteConsent(
        @Path("consent_id") consentId: String
    )
    
    /**
     * Remove patient from doctor's patient list
     * 
     * @param doctorId Doctor ID
     * @param patientId Patient ID
     */
    @DELETE("doctors/{doctor_id}/patients/{patient_id}")
    suspend fun removePatient(
        @Path("doctor_id") doctorId: String,
        @Path("patient_id") patientId: String
    ): retrofit2.Response<Unit>

    @POST("hospitals/{hospital_id}/admit")
    suspend fun admitPatient(
        @Path("hospital_id") hospitalId: String,
        @Body request: AdmitPatientRequest
    ): AdmitPatientResponse

    @GET("doctors/{doctor_id}/hospitals")
    suspend fun getDoctorHospitals(
        @Path("doctor_id") doctorId: String
    ): retrofit2.Response<List<HospitalAssociation>>

    @POST("doctors/{doctor_id}/hospitals")
    suspend fun updateDoctorHospitals(
        @Path("doctor_id") doctorId: String,
        @Body request: UpdateDoctorHospitalsRequest
    ): retrofit2.Response<Map<String, Any>>
}


