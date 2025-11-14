package com.example.cloudcareapp.data.model

data class Patient(
    val id: Int,
    val name: String,
    val age: Int,
    val gender: String,
    val bloodType: String,
    val contact: String,
    val email: String,
    val address: String,
    val familyContact: String,
    val insuranceProvider: String,
    val insuranceId: String,
    val emergency: Boolean = false,
    val occupation: String = "",
    val aiAnalysis: String? = null
)

data class WearableDevice(
    val id: Int,
    val name: String,
    val type: String,
    val isConnected: Boolean,
    val batteryLevel: Int,
    val lastSyncTime: String,
    val dataPointsSynced: Int,
    val iconType: DeviceIconType = DeviceIconType.FITNESS_TRACKER
)

enum class DeviceIconType {
    FITNESS_TRACKER,
    SMART_WATCH,
    HEART_MONITOR,
    BLOOD_PRESSURE
}

data class WearableData(
    val id: Int,
    val timestamp: String,
    val heartRate: Int?,
    val steps: Int?,
    val sleepHours: Double?,
    val calories: Int?,
    val oxygenLevel: Int?,
    val bloodPressureSystolic: Int? = null,
    val bloodPressureDiastolic: Int? = null
)

data class HealthInsight(
    val id: Int,
    val title: String,
    val value: String,
    val subtitle: String? = null,
    val type: InsightType,
    val trend: String? = null
)

enum class InsightType {
    STEPS,
    HEART_RATE,
    SLEEP,
    CALORIES
}

data class HealthSummary(
    val steps: Int,
    val stepsChange: Int, // percentage
    val heartRate: Int,
    val heartRateStatus: String, // "Normal", "Elevated", etc.
    val sleepHours: Double,
    val sleepChange: Int, // percentage
    val calories: Int,
    val caloriesPercentage: Int, // of goal
    val caloriesGoal: Int = 2000
)
