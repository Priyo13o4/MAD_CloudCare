package com.example.cloudcareapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Document Upload Models
 */
data class DocumentUploadRequest(
    @SerializedName("patient_id")
    val patientId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("record_type")
    val recordType: String, // LAB_REPORT, PRESCRIPTION, GENERAL, CONSULTATION, IMAGING
    @SerializedName("file_base64")
    val fileBase64: String, // Base64 encoded file
    @SerializedName("file_name")
    val fileName: String,
    @SerializedName("file_type")
    val fileType: String // pdf, jpg, png, etc.
)

data class DocumentUploadResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("document_id")
    val documentId: String?,
    @SerializedName("document_url")
    val documentUrl: String?,
    @SerializedName("message")
    val message: String?
)

data class DocumentsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("documents")
    val documents: List<DocumentItem>?,
    @SerializedName("message")
    val message: String?
)

data class DocumentItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("record_type")
    val recordType: String,
    @SerializedName("file_url")
    val fileUrl: String?,
    @SerializedName("date")
    val date: String,
    @SerializedName("created_at")
    val createdAt: String
)

/**
 * Doctor Profile Models - Matches schema.prisma exactly
 */
data class DoctorProfileResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("doctor")
    val doctor: DoctorProfileData?,
    @SerializedName("message")
    val message: String?
)

data class DoctorProfileData(
    @SerializedName("id")
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("specialization")
    val specialization: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("department")
    val department: String,
    @SerializedName("join_date")
    val joinDate: String,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("hospital_id")
    val hospitalId: String?
)

data class UpdateDoctorProfileRequest(
    @SerializedName("name")
    val name: String?,
    @SerializedName("specialization")
    val specialization: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("department")
    val department: String?
)

/**
 * Patient Profile Models - Matches schema.prisma exactly
 */
data class PatientProfileResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("patient")
    val patient: PatientProfileData?,
    @SerializedName("message")
    val message: String?
)

data class PatientProfileData(
    @SerializedName("id")
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("aadhar_uid")
    val aadharUid: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("age")
    val age: Int,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("blood_type")
    val bloodType: String,
    @SerializedName("contact")
    val contact: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("family_contact")
    val familyContact: String,
    @SerializedName("insurance_provider")
    val insuranceProvider: String?,
    @SerializedName("insurance_id")
    val insuranceId: String?,
    @SerializedName("emergency")
    val emergency: Boolean,
    @SerializedName("occupation")
    val occupation: String?,
    @SerializedName("ai_analysis")
    val aiAnalysis: String?
)

data class UpdatePatientProfileRequest(
    @SerializedName("name")
    val name: String?,
    @SerializedName("age")
    val age: Int?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("blood_type")
    val bloodType: String?,
    @SerializedName("contact")
    val contact: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("address")
    val address: String?,
    @SerializedName("family_contact")
    val familyContact: String?,
    @SerializedName("insurance_provider")
    val insuranceProvider: String?,
    @SerializedName("insurance_id")
    val insuranceId: String?,
    @SerializedName("occupation")
    val occupation: String?
)

/**
 * Notifications Models
 */
data class NotificationsResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("notifications")
    val notifications: List<NotificationItem>?,
    @SerializedName("unread_count")
    val unreadCount: Int,
    @SerializedName("message")
    val message: String?
)

data class NotificationItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("type")
    val type: String, // EMERGENCY, APPOINTMENT, PATIENT_UPDATE, SYSTEM
    @SerializedName("is_read")
    val isRead: Boolean,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("related_id")
    val relatedId: String? // Patient ID, Appointment ID, etc.
)
