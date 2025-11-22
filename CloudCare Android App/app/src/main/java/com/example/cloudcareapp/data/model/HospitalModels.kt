package com.example.cloudcareapp.data.model

data class HospitalStats(
    val admittedPatients: Int,
    val availableDoctors: Int,
    val emergencyCases: Int,
    val avgResponseTime: String,
    val totalBeds: Int,
    val availableBeds: Int
)

data class EmergencyCase(
    val id: String,
    val patientId: String,
    val patientName: String,
    val age: Int,
    val gender: String,
    val condition: String,
    val severity: EmergencySeverity,
    val status: EmergencyStatus,
    val admittedTime: String,
    val assignedDoctor: String,
    val department: String
)

enum class EmergencySeverity {
    CRITICAL, HIGH, MEDIUM, LOW
}

enum class EmergencyStatus {
    IN_TREATMENT, STABLE, WAITING, DISCHARGED
}

data class HospitalStaff(
    val id: String,
    val name: String,
    val age: Int,
    val specialization: String,
    val department: String,
    val patientCount: Int,
    val status: StaffStatus,
    val joinDate: String,
    val email: String,
    val phone: String
)

enum class StaffStatus {
    ACTIVE, ON_LEAVE, UNAVAILABLE
}

data class Department(
    val id: String,
    val name: String,
    val totalBeds: Int,
    val occupiedBeds: Int,
    val headDoctor: String,
    val status: DepartmentStatus
)

enum class DepartmentStatus {
    NORMAL, BUSY, CRITICAL
}

data class HospitalResource(
    val id: String,
    val name: String,
    val category: ResourceCategory,
    val total: Int,
    val available: Int,
    val inUse: Int,
    val status: ResourceStatus
)

enum class ResourceCategory {
    BEDS, EQUIPMENT, SUPPLIES, MEDICATION
}

enum class ResourceStatus {
    NORMAL, LOW, CRITICAL
}

// ==================== API Response Models ====================

data class HospitalProfileResponse(
    val id: String,
    @com.google.gson.annotations.SerializedName("user_id") val userId: String,
    val name: String,
    val email: String,
    @com.google.gson.annotations.SerializedName("phone_primary") val phonePrimary: String?,
    @com.google.gson.annotations.SerializedName("phone_emergency") val phoneEmergency: String?,
    @com.google.gson.annotations.SerializedName("registration_no") val registrationNo: String?,
    @com.google.gson.annotations.SerializedName("hospital_code") val hospitalCode: String?,
    @com.google.gson.annotations.SerializedName("address_line1") val addressLine1: String?,
    @com.google.gson.annotations.SerializedName("address_line2") val addressLine2: String?,
    val city: String?,
    val state: String?,
    @com.google.gson.annotations.SerializedName("postal_code") val postalCode: String?,
    @com.google.gson.annotations.SerializedName("total_beds") val totalBeds: Int,
    @com.google.gson.annotations.SerializedName("available_beds") val availableBeds: Int,
    @com.google.gson.annotations.SerializedName("icu_beds") val icuBeds: Int = 0,
    @com.google.gson.annotations.SerializedName("emergency_beds") val emergencyBeds: Int = 0,
    @com.google.gson.annotations.SerializedName("oxygen_cylinders") val oxygenCylinders: Int = 0,
    @com.google.gson.annotations.SerializedName("ventilators") val ventilators: Int = 0,
    @com.google.gson.annotations.SerializedName("ambulances") val ambulances: Int = 0,
    @com.google.gson.annotations.SerializedName("blood_bags") val bloodBags: Int = 0,
    @com.google.gson.annotations.SerializedName("total_doctors") val totalDoctors: Int,
    val specializations: String?,
    @com.google.gson.annotations.SerializedName("created_at") val createdAt: String
)

data class HospitalDashboardStats(
    @com.google.gson.annotations.SerializedName("total_patients") val totalPatients: Int,
    @com.google.gson.annotations.SerializedName("total_doctors") val totalDoctors: Int,
    @com.google.gson.annotations.SerializedName("emergency_cases") val emergencyCases: Int,
    @com.google.gson.annotations.SerializedName("available_beds") val availableBeds: Int,
    @com.google.gson.annotations.SerializedName("total_beds") val totalBeds: Int,
    @com.google.gson.annotations.SerializedName("occupancy_rate") val occupancyRate: Float
)

data class DoctorSummary(
    val id: String,
    val name: String,
    val specialization: String,
    @com.google.gson.annotations.SerializedName("is_available") val isAvailable: Boolean,
    val phone: String?
)

data class PatientSummary(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val status: String,
    @com.google.gson.annotations.SerializedName("last_visit") val lastVisit: String?
)

data class ResourceUpdate(
    @com.google.gson.annotations.SerializedName("total_beds") val totalBeds: Int? = null,
    @com.google.gson.annotations.SerializedName("available_beds") val availableBeds: Int? = null,
    @com.google.gson.annotations.SerializedName("icu_beds") val icuBeds: Int? = null,
    @com.google.gson.annotations.SerializedName("emergency_beds") val emergencyBeds: Int? = null,
    @com.google.gson.annotations.SerializedName("oxygen_cylinders") val oxygenCylinders: Int? = null,
    @com.google.gson.annotations.SerializedName("ventilators") val ventilators: Int? = null,
    @com.google.gson.annotations.SerializedName("ambulances") val ambulances: Int? = null,
    @com.google.gson.annotations.SerializedName("blood_bags") val bloodBags: Int? = null
)

data class ResourceUpdateResponse(
    val success: Boolean,
    val message: String,
    val data: Any?
)

data class RecordLookupRequest(
    @com.google.gson.annotations.SerializedName("hospital_id") val hospitalId: String,
    @com.google.gson.annotations.SerializedName("aadhar_number") val aadharNumber: String
)

data class MedicalRecordSummary(
    val id: String,
    val title: String,
    val date: String,
    val type: String,
    val description: String,
    @com.google.gson.annotations.SerializedName("doctor_name") val doctorName: String?
)

data class AdmitPatientRequest(
    @com.google.gson.annotations.SerializedName("aadhar_number") val aadharNumber: String? = null,
    @com.google.gson.annotations.SerializedName("patient_id") val patientId: String? = null,
    val reason: String = "Hospital Admission"
)

data class AdmitPatientResponse(
    val success: Boolean,
    val message: String,
    @com.google.gson.annotations.SerializedName("consent_id") val consentId: String?
)

data class HospitalSearchResult(
    val id: String,
    val name: String,
    @com.google.gson.annotations.SerializedName("hospital_code") val hospitalCode: String,
    val city: String?,
    val state: String?
)

data class HospitalAssociation(
    val id: String,
    @com.google.gson.annotations.SerializedName("hospital_id") val hospitalId: String,
    @com.google.gson.annotations.SerializedName("hospital_name") val hospitalName: String,
    @com.google.gson.annotations.SerializedName("hospital_code") val hospitalCode: String,
    @com.google.gson.annotations.SerializedName("is_primary") val isPrimary: Boolean,
    @com.google.gson.annotations.SerializedName("joined_at") val joinedAt: String
)

data class UpdateDoctorHospitalsRequest(
    @com.google.gson.annotations.SerializedName("hospital_ids") val hospitalIds: List<String>,
    @com.google.gson.annotations.SerializedName("primary_hospital_id") val primaryHospitalId: String? = null
)
