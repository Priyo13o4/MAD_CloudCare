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
