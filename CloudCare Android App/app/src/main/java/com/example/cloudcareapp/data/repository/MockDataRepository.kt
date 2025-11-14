package com.example.cloudcareapp.data.repository

import com.example.cloudcareapp.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object MockDataRepository {
    
    // Mock Patient Data
    private val mockPatient = Patient(
        id = 7,
        name = "Mobile Test User",
        age = 28,
        gender = "Male",
        bloodType = "O+",
        contact = "+91 98765 43210",
        email = "patient7@cloudcare.local",
        address = "123 Medical Street, Bangalore, Karnataka",
        familyContact = "+91 98765 43211 (Father)",
        insuranceProvider = "Star Health Insurance",
        insuranceId = "SH789456123",
        emergency = false,
        occupation = "Software Engineer"
    )
    
    // Mock Wearable Devices
    private val mockDevices = listOf(
        WearableDevice(
            id = 1,
            name = "Xiaomi Mi Band",
            type = "Fitness Tracker",
            isConnected = true,
            batteryLevel = 85,
            lastSyncTime = "2 minutes ago",
            dataPointsSynced = 7,
            iconType = DeviceIconType.FITNESS_TRACKER
        )
    )
    
    // Mock Health Summary
    private val mockHealthSummary = HealthSummary(
        steps = 4932,
        stepsChange = 12,
        heartRate = 76,
        heartRateStatus = "Normal",
        sleepHours = 0.0,
        sleepChange = 8,
        calories = 1877,
        caloriesPercentage = 94,
        caloriesGoal = 2000
    )
    
    // Mock Health Insights
    private val mockInsights = listOf(
        HealthInsight(
            id = 1,
            title = "Steps Trend",
            value = "Average 6066 steps/day this week",
            type = InsightType.STEPS
        ),
        HealthInsight(
            id = 2,
            title = "Heart Health",
            value = "Average resting HR: 73 bpm",
            type = InsightType.HEART_RATE
        ),
        HealthInsight(
            id = 3,
            title = "Sleep Quality",
            value = "Average 0h sleep per night",
            type = InsightType.SLEEP
        )
    )
    
    // Mock Facilities
    private val mockFacilities = listOf(
        Facility(
            id = 1,
            name = "Archana Eye Clinic",
            type = FacilityType.CLINIC,
            patientId = "22343",
            iconType = FacilityIconType.EYE_CLINIC
        ),
        Facility(
            id = 2,
            name = "Dr Lal Pathlabs NRL-HIP",
            type = FacilityType.LAB,
            patientId = "REF00117-25",
            iconType = FacilityIconType.LAB
        ),
        Facility(
            id = 3,
            name = "Kidney Center Hospital",
            type = FacilityType.HOSPITAL,
            patientId = "22585",
            iconType = FacilityIconType.HOSPITAL
        )
    )
    
    // Mock Recent Activities
    private val mockActivities = listOf(
        Activity(
            id = 1,
            title = "Health record shared with Archana Eye Clinic",
            description = "",
            timestamp = "2 hours ago",
            type = ActivityType.RECORD_SHARED
        ),
        Activity(
            id = 2,
            title = "New consent request from Dr Lal Pathlabs",
            description = "",
            timestamp = "1 day ago",
            type = ActivityType.CONSENT_REQUEST
        ),
        Activity(
            id = 3,
            title = "Fitbit data synced successfully",
            description = "",
            timestamp = "2 days ago",
            type = ActivityType.DATA_SYNCED
        ),
        Activity(
            id = 4,
            title = "Lab report uploaded",
            description = "",
            timestamp = "3 days ago",
            type = ActivityType.LAB_REPORT_UPLOADED
        )
    )
    
    // Mock Dashboard Stats
    private val mockStats = DashboardStats(
        linkedFacilities = 5,
        healthRecords = 23,
        pendingConsents = 3,
        connectedDevices = 2
    )
    
    // Mock Consents
    private val mockConsents = listOf(
        Consent(
            id = 1,
            facilityName = "Dr Lal Pathlabs",
            requestType = "Requesting access to lab results",
            timestamp = "1 day ago",
            status = ConsentStatus.PENDING,
            description = "Access to view and download your recent blood test results"
        ),
        Consent(
            id = 2,
            facilityName = "Archana Eye Clinic",
            requestType = "Requesting medical history",
            timestamp = "2 days ago",
            status = ConsentStatus.PENDING,
            description = "Access to your medical history for consultation"
        ),
        Consent(
            id = 3,
            facilityName = "Kidney Center Hospital",
            requestType = "Requesting wearable data access",
            timestamp = "3 days ago",
            status = ConsentStatus.PENDING,
            description = "Access to your fitness and health tracking data"
        ),
        Consent(
            id = 4,
            facilityName = "Apollo Hospital",
            requestType = "Full medical records",
            timestamp = "1 week ago",
            status = ConsentStatus.APPROVED,
            description = "Complete access to medical records"
        ),
        Consent(
            id = 5,
            facilityName = "Fortis Hospital",
            requestType = "Prescription access",
            timestamp = "2 weeks ago",
            status = ConsentStatus.APPROVED,
            description = "Access to current prescriptions"
        )
    )
    
    // API Methods
    suspend fun getPatient(id: Int): Patient {
        delay(300) // Simulate network delay
        return mockPatient
    }
    
    suspend fun getWearableDevices(): List<WearableDevice> {
        delay(200)
        return mockDevices
    }
    
    suspend fun getHealthSummary(): HealthSummary {
        delay(200)
        return mockHealthSummary
    }
    
    suspend fun getHealthInsights(): List<HealthInsight> {
        delay(200)
        return mockInsights
    }
    
    suspend fun getLinkedFacilities(): List<Facility> {
        delay(200)
        return mockFacilities
    }
    
    suspend fun getRecentActivities(): List<Activity> {
        delay(200)
        return mockActivities
    }
    
    suspend fun getDashboardStats(): DashboardStats {
        delay(200)
        return mockStats
    }
    
    suspend fun getConsents(): List<Consent> {
        delay(200)
        return mockConsents
    }
    
    suspend fun getPendingConsents(): List<Consent> {
        delay(200)
        return mockConsents.filter { it.status == ConsentStatus.PENDING }
    }
    
    suspend fun getApprovedConsents(): List<Consent> {
        delay(200)
        return mockConsents.filter { it.status == ConsentStatus.APPROVED }
    }
    
    suspend fun getMedicalRecords(): List<MedicalRecord> {
        delay(300)
        // Return empty for now (showing error state in UI)
        return emptyList()
    }
    
    // Flow-based methods for reactive updates
    fun getPatientFlow(id: Int): Flow<Patient> = flow {
        emit(getPatient(id))
    }
    
    fun getHealthSummaryFlow(): Flow<HealthSummary> = flow {
        emit(getHealthSummary())
    }
}
