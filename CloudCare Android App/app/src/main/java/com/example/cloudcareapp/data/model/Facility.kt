package com.example.cloudcareapp.data.model

data class Facility(
    val id: Int,
    val name: String,
    val type: FacilityType,
    val patientId: String,
    val iconType: FacilityIconType
)

enum class FacilityType {
    HOSPITAL,
    CLINIC,
    LAB,
    PHARMACY
}

enum class FacilityIconType {
    EYE_CLINIC,
    LAB,
    HOSPITAL,
    GENERAL
}

data class MedicalRecord(
    val id: Int,
    val patientId: Int,
    val title: String,
    val description: String,
    val date: String,
    val facilityName: String? = null,
    val recordType: RecordType = RecordType.GENERAL
)

enum class RecordType {
    LAB_REPORT,
    PRESCRIPTION,
    GENERAL,
    CONSULTATION,
    IMAGING
}

data class Activity(
    val id: Int,
    val title: String,
    val description: String,
    val timestamp: String,
    val type: ActivityType
)

enum class ActivityType {
    RECORD_SHARED,
    CONSENT_REQUEST,
    DATA_SYNCED,
    LAB_REPORT_UPLOADED,
    APPOINTMENT_SCHEDULED,
    GENERAL
}

data class Consent(
    val id: Int,
    val facilityName: String,
    val requestType: String,
    val timestamp: String,
    val status: ConsentStatus,
    val description: String = ""
)

enum class ConsentStatus {
    PENDING,
    APPROVED,
    DENIED
}

data class DashboardStats(
    val linkedFacilities: Int,
    val healthRecords: Int,
    val pendingConsents: Int,
    val connectedDevices: Int
)
