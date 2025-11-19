package com.example.cloudcareapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * QR Code Data Model
 * Contains patient ID that will be encoded in QR code
 */
data class PatientQRData(
    @SerializedName("patient_id")
    val patientId: String,
    @SerializedName("type")
    val type: String = "patient_health_record",
    @SerializedName("api_version")
    val apiVersion: String = "v1",
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Complete Patient Data Response
 * Returned when doctor scans QR code
 */
data class PatientHealthRecordResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: PatientCompleteData?,
    @SerializedName("message")
    val message: String?
)

/**
 * Complete patient data including all health information
 */
data class PatientCompleteData(
    @SerializedName("patient")
    val patient: Patient,
    @SerializedName("wearables_summary")
    val wearablesSummary: HealthSummary?,
    @SerializedName("recent_metrics")
    val recentMetrics: List<HealthMetric>?,
    @SerializedName("devices")
    val devices: List<WearableDevice>?,
    @SerializedName("health_records")
    val healthRecords: List<MedicalRecord>?,
    @SerializedName("last_updated")
    val lastUpdated: String
)
