package com.example.cloudcareapp.data.model

import com.google.gson.annotations.SerializedName

data class DoctorStats(
    val totalPatients: Int,
    val todaysAppointments: Int,
    val activeAlerts: Int,
    val pendingReports: Int
)

data class EmergencyAlert(
    val id: String,
    val patientId: String,
    val patientName: String,
    val severity: AlertSeverity,
    val alertType: AlertType,
    val message: String,
    val timestamp: String,
    val currentValue: String = ""
)

enum class AlertSeverity {
    CRITICAL, HIGH, MEDIUM, LOW
}

enum class AlertType {
    HEART_RATE, OXYGEN_LEVEL, BLOOD_PRESSURE, TEMPERATURE, OTHER
}

data class AssignedPatient(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val status: PatientStatus,
    val condition: String,
    val nextAppointment: String,
    val lastVisit: String,
    val emergencyFlag: Boolean = false
)

enum class PatientStatus {
    STABLE, MONITORING, CRITICAL
}

data class DoctorAppointment(
    val id: String,
    val patientName: String,
    val patientId: String,
    val time: String,
    val date: String,
    val type: String,
    val department: String,
    val reason: String,
    val status: AppointmentStatus,
    val notes: String = ""
)

enum class AppointmentStatus {
    SCHEDULED, COMPLETED, CANCELLED, IN_PROGRESS
}

data class PatientRecord(
    val id: String,
    val patientId: String,
    val patientName: String,
    val recordType: DoctorRecordType,
    val title: String,
    val date: String,
    val description: String,
    val diagnosis: String = "",
    val prescriptions: String = "",
    val doctorNotes: String = "",
    val testResults: String = ""
)

enum class DoctorRecordType {
    CHECKUP, LAB_RESULT, PRESCRIPTION, DIAGNOSIS, SURGERY, EMERGENCY
}

// ==================== API Response Models ====================

data class DoctorProfileResponse(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("middle_name") val middleName: String?,
    @SerializedName("last_name") val lastName: String?,
    val title: String?,
    val specialization: String,
    @SerializedName("sub_specialization") val subSpecialization: String?,
    @SerializedName("medical_license_no") val medicalLicenseNo: String?,
    @SerializedName("phone_primary") val phonePrimary: String?,
    @SerializedName("email_professional") val emailProfessional: String?,
    val qualifications: String?,
    @SerializedName("experience_years") val experienceYears: Int?,
    val city: String?,
    val state: String?,
    @SerializedName("created_at") val createdAt: String
)

data class UpdateDoctorProfileRequest(
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    val specialization: String? = null,
    @SerializedName("phone_primary") val phonePrimary: String? = null
)

data class DoctorPatientResponse(
    val id: String,
    @SerializedName("patient_id") val patientId: String,
    @SerializedName("patient_name") val patientName: String,
    val status: String, // "STABLE", "MONITORING", "CRITICAL"
    val condition: String,
    @SerializedName("next_appointment") val nextAppointment: String?,
    @SerializedName("last_visit") val lastVisit: String?,
    @SerializedName("emergency_flag") val emergencyFlag: Boolean,
    @SerializedName("assigned_at") val assignedAt: String,
    @SerializedName("access_granted") val accessGranted: Boolean,
    
    // Details
    @SerializedName("patient_age") val patientAge: Int?,
    @SerializedName("patient_gender") val patientGender: String?,
    @SerializedName("patient_blood_group") val patientBloodGroup: String?,
    @SerializedName("patient_phone") val patientPhone: String?
)
