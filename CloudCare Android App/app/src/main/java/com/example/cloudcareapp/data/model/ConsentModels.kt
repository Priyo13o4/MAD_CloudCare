package com.example.cloudcareapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Consent Request Model
 * Used when doctor scans patient QR code
 */
data class CreateConsentRequest(
    @SerializedName("patient_id")
    val patientId: String,
    @SerializedName("doctor_id")
    val doctorId: String,
    @SerializedName("facility_name")
    val facilityName: String,
    @SerializedName("request_type")
    val requestType: String = "DATA_ACCESS",
    @SerializedName("description")
    val description: String = "Request to access your medical records",
    @SerializedName("expires_in_days")
    val expiresInDays: Int = 90
)

/**
 * Consent Response Model
 */
data class ConsentResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("patient_id")
    val patientId: String,
    @SerializedName("facility_name")
    val facilityName: String,
    @SerializedName("request_type")
    val requestType: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("status")
    val status: String, // PENDING, APPROVED, DENIED
    @SerializedName("requested_at")
    val requestedAt: String,
    @SerializedName("responded_at")
    val respondedAt: String?,
    @SerializedName("expires_at")
    val expiresAt: String?
)

/**
 * Update Consent Request Model
 * Used when patient approves/denies consent
 */
data class UpdateConsentRequest(
    @SerializedName("status")
    val status: String // APPROVED or DENIED
)

/**
 * List of Consent Requests
 */
data class ConsentListResponse(
    @SerializedName("consents")
    val consents: List<ConsentResponse>
)
