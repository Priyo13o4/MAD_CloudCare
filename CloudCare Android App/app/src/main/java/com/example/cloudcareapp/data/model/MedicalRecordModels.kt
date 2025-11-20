package com.example.cloudcareapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Medical Record Request/Response Models
 * Matches backend /api/v1/documents endpoints
 */

data class CreateMedicalRecordRequest(
    @SerializedName("patient_id")
    val patientId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("record_type")
    val recordType: String,
    @SerializedName("facility_id")
    val facilityId: String? = null,
    @SerializedName("file_data")
    val fileData: String? = null // Base64 encoded file
)

data class MedicalRecordResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("patient_id")
    val patientId: String,
    @SerializedName("facility_id")
    val facilityId: String?,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("record_type")
    val recordType: String,
    @SerializedName("file_url")
    val fileUrl: String?,
    @SerializedName("created_at")
    val createdAt: String
)
