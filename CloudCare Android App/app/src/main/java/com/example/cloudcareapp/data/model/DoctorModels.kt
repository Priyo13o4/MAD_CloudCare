package com.example.cloudcareapp.data.model

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
