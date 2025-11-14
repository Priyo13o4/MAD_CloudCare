package com.example.cloudcareapp.data

import com.example.cloudcareapp.data.model.*

object MockDoctorData {
    
    val MOCK_DOCTOR_STATS = DoctorStats(
        totalPatients = 127,
        todaysAppointments = 12,
        activeAlerts = 8,
        pendingReports = 5
    )
    
    val MOCK_EMERGENCY_ALERTS = listOf(
        EmergencyAlert(
            id = "alert_001",
            patientId = "P-003",
            patientName = "Michael Brown",
            severity = AlertSeverity.CRITICAL,
            alertType = AlertType.HEART_RATE,
            message = "Abnormal heart rate detected",
            timestamp = "5 mins ago",
            currentValue = "145 BPM"
        ),
        EmergencyAlert(
            id = "alert_002",
            patientId = "P-004",
            patientName = "David Wilson",
            severity = AlertSeverity.HIGH,
            alertType = AlertType.OXYGEN_LEVEL,
            message = "Low oxygen saturation - 89%",
            timestamp = "15 mins ago",
            currentValue = "89%"
        ),
        EmergencyAlert(
            id = "alert_003",
            patientId = "P-005",
            patientName = "Jennifer Martinez",
            severity = AlertSeverity.MEDIUM,
            alertType = AlertType.BLOOD_PRESSURE,
            message = "Blood pressure spike - 165/95",
            timestamp = "30 mins ago",
            currentValue = "165/95 mmHg"
        )
    )
    
    val MOCK_ASSIGNED_PATIENTS = listOf(
        AssignedPatient(
            id = "P-001",
            name = "John Smith",
            age = 45,
            gender = "M",
            status = PatientStatus.STABLE,
            condition = "Hypertension",
            nextAppointment = "Oct 22, 2025",
            lastVisit = "Oct 15, 2025",
            emergencyFlag = false
        ),
        AssignedPatient(
            id = "P-002",
            name = "Emily Davis",
            age = 32,
            gender = "F",
            status = PatientStatus.MONITORING,
            condition = "Diabetes Type 2",
            nextAppointment = "Oct 21, 2025",
            lastVisit = "Oct 14, 2025",
            emergencyFlag = false
        ),
        AssignedPatient(
            id = "P-003",
            name = "Michael Brown",
            age = 58,
            gender = "M",
            status = PatientStatus.CRITICAL,
            condition = "Cardiac Arrhythmia",
            nextAppointment = "Emergency",
            lastVisit = "Oct 18, 2025",
            emergencyFlag = true
        ),
        AssignedPatient(
            id = "P-004",
            name = "David Wilson",
            age = 67,
            gender = "M",
            status = PatientStatus.MONITORING,
            condition = "COPD",
            nextAppointment = "Oct 23, 2025",
            lastVisit = "Oct 16, 2025",
            emergencyFlag = false
        ),
        AssignedPatient(
            id = "P-005",
            name = "Jennifer Martinez",
            age = 41,
            gender = "F",
            status = PatientStatus.STABLE,
            condition = "Hypertension",
            nextAppointment = "Oct 25, 2025",
            lastVisit = "Oct 10, 2025",
            emergencyFlag = false
        )
    )
    
    val MOCK_DOCTOR_APPOINTMENTS = listOf(
        DoctorAppointment(
            id = "apt_001",
            patientName = "John Smith",
            patientId = "P-001",
            time = "09:00 AM",
            date = "Nov 09, 2025",
            type = "Routine Checkup",
            department = "Cardiology",
            reason = "Blood pressure monitoring",
            status = AppointmentStatus.SCHEDULED,
            notes = "Regular follow-up appointment"
        ),
        DoctorAppointment(
            id = "apt_002",
            patientName = "Michael Brown",
            patientId = "P-003",
            time = "10:30 AM",
            date = "Nov 09, 2025",
            type = "Emergency",
            department = "Cardiology",
            reason = "Abnormal heart rate",
            status = AppointmentStatus.IN_PROGRESS,
            notes = "Urgent attention required"
        ),
        DoctorAppointment(
            id = "apt_003",
            patientName = "Emily Davis",
            patientId = "P-002",
            time = "11:00 AM",
            date = "Nov 09, 2025",
            type = "Follow-up",
            department = "Endocrinology",
            reason = "Diabetes management",
            status = AppointmentStatus.SCHEDULED,
            notes = "Review recent test results"
        ),
        DoctorAppointment(
            id = "apt_004",
            patientName = "Sarah Johnson",
            patientId = "P-006",
            time = "02:00 PM",
            date = "Nov 09, 2025",
            type = "Consultation",
            department = "Cardiology",
            reason = "Chest pain evaluation",
            status = AppointmentStatus.SCHEDULED
        ),
        DoctorAppointment(
            id = "apt_005",
            patientName = "David Wilson",
            patientId = "P-004",
            time = "03:30 PM",
            date = "Nov 09, 2025",
            type = "Follow-up",
            department = "Pulmonology",
            reason = "COPD progress check",
            status = AppointmentStatus.SCHEDULED
        ),
        DoctorAppointment(
            id = "apt_006",
            patientName = "Robert Lee",
            patientId = "P-007",
            time = "08:30 AM",
            date = "Nov 08, 2025",
            type = "Routine Checkup",
            department = "Cardiology",
            reason = "Annual physical",
            status = AppointmentStatus.COMPLETED
        )
    )
    
    val MOCK_PATIENT_RECORDS = listOf(
        PatientRecord(
            id = "REC-001",
            patientId = "P-001",
            patientName = "John Smith",
            recordType = DoctorRecordType.CHECKUP,
            title = "Routine Cardiac Checkup",
            date = "Nov 08, 2025",
            description = "Regular follow-up for hypertension management",
            diagnosis = "Hypertension (Stage 1) - Well controlled",
            prescriptions = "Lisinopril 10mg daily, Continue current regimen",
            doctorNotes = "Blood pressure stable at 128/82. Patient compliant with medication. Advised to maintain low-sodium diet and regular exercise.",
            testResults = "BP: 128/82, Heart Rate: 72 bpm, ECG: Normal sinus rhythm"
        ),
        PatientRecord(
            id = "REC-002",
            patientId = "P-003",
            patientName = "Michael Brown",
            recordType = DoctorRecordType.EMERGENCY,
            title = "Emergency - Cardiac Arrhythmia",
            date = "Nov 09, 2025",
            description = "Patient presented with abnormal heart rate and chest discomfort",
            diagnosis = "Atrial Fibrillation - Acute episode",
            prescriptions = "Metoprolol 50mg twice daily, Warfarin 5mg daily",
            doctorNotes = "Patient admitted to ICU. Heart rate elevated to 145 bpm. Started on beta-blockers. Requires continuous monitoring. Cardiology consult recommended.",
            testResults = "ECG: Atrial fibrillation, Heart Rate: 145 bpm, Troponin: Elevated"
        ),
        PatientRecord(
            id = "REC-003",
            patientId = "P-002",
            patientName = "Emily Davis",
            recordType = DoctorRecordType.LAB_RESULT,
            title = "Diabetes Management - Lab Results",
            date = "Nov 07, 2025",
            description = "Quarterly HbA1c and glucose monitoring",
            diagnosis = "Type 2 Diabetes Mellitus - Moderate control",
            prescriptions = "Metformin 1000mg twice daily, Increase insulin dosage",
            doctorNotes = "HbA1c shows improvement from 8.2% to 7.4%. Glucose levels still fluctuating. Adjusted insulin dosage. Schedule follow-up in 6 weeks.",
            testResults = "HbA1c: 7.4%, Fasting Glucose: 142 mg/dL, Postprandial: 188 mg/dL"
        ),
        PatientRecord(
            id = "REC-004",
            patientId = "P-004",
            patientName = "David Wilson",
            recordType = DoctorRecordType.PRESCRIPTION,
            title = "COPD Medication Refill",
            date = "Nov 06, 2025",
            description = "Routine medication renewal and pulmonary function assessment",
            diagnosis = "Chronic Obstructive Pulmonary Disease (Moderate)",
            prescriptions = "Albuterol inhaler 2 puffs as needed, Tiotropium 18mcg daily, Prednisone 10mg (if exacerbation)",
            doctorNotes = "Patient reports improvement in breathing. SpO2 stable at 94%. No recent exacerbations. Continue current regimen. Encouraged smoking cessation.",
            testResults = "SpO2: 94%, FEV1: 58% predicted, Chest X-ray: Stable"
        ),
        PatientRecord(
            id = "REC-005",
            patientId = "P-005",
            patientName = "Jennifer Martinez",
            recordType = DoctorRecordType.DIAGNOSIS,
            title = "Hypertension Follow-up",
            date = "Nov 05, 2025",
            description = "Blood pressure spike investigation",
            diagnosis = "Essential Hypertension - Uncontrolled",
            prescriptions = "Amlodipine 10mg daily, Hydrochlorothiazide 25mg daily",
            doctorNotes = "Blood pressure elevated at 165/95. Added second antihypertensive. Patient advised on dietary modifications and stress management. Follow-up in 2 weeks.",
            testResults = "BP: 165/95, BMP: Normal, Kidney function: Normal"
        ),
        PatientRecord(
            id = "REC-006",
            patientId = "P-001",
            patientName = "John Smith",
            recordType = DoctorRecordType.LAB_RESULT,
            title = "Lipid Panel Results",
            date = "Oct 28, 2025",
            description = "Annual lipid profile screening",
            diagnosis = "Hyperlipidemia",
            prescriptions = "Atorvastatin 20mg at bedtime",
            doctorNotes = "LDL slightly elevated. Started on statin therapy. Advised dietary changes - reduce saturated fats. Repeat lipid panel in 3 months.",
            testResults = "Total Cholesterol: 245 mg/dL, LDL: 165 mg/dL, HDL: 42 mg/dL, Triglycerides: 190 mg/dL"
        ),
        PatientRecord(
            id = "REC-007",
            patientId = "P-002",
            patientName = "Emily Davis",
            recordType = DoctorRecordType.CHECKUP,
            title = "Diabetes Quarterly Review",
            date = "Oct 20, 2025",
            description = "Routine diabetes management consultation",
            diagnosis = "Type 2 Diabetes Mellitus",
            prescriptions = "Metformin 1000mg twice daily, Insulin glargine 20 units at bedtime",
            doctorNotes = "Patient compliant with medications. Blood sugar logs reviewed. Some spikes noted after evening meals. Adjusted insulin timing. Continue monitoring.",
            testResults = "Random Glucose: 156 mg/dL, BP: 130/84"
        ),
        PatientRecord(
            id = "REC-008",
            patientId = "P-003",
            patientName = "Michael Brown",
            recordType = DoctorRecordType.CHECKUP,
            title = "Cardiology Follow-up",
            date = "Oct 15, 2025",
            description = "Post-diagnosis cardiac monitoring",
            diagnosis = "Cardiac Arrhythmia - Stable",
            prescriptions = "Metoprolol 25mg twice daily",
            doctorNotes = "Heart rate controlled. No recent episodes of palpitations. ECG shows normal rhythm. Patient educated on warning signs. Continue current therapy.",
            testResults = "ECG: Normal sinus rhythm, Heart Rate: 68 bpm, BP: 122/78"
        )
    )
    
    fun getTodaysAppointments(): List<DoctorAppointment> {
        return MOCK_DOCTOR_APPOINTMENTS.filter { it.date == "Nov 09, 2025" }
    }
    
    fun getUpcomingAppointments(): List<DoctorAppointment> {
        return MOCK_DOCTOR_APPOINTMENTS.filter { 
            it.status == AppointmentStatus.SCHEDULED || it.status == AppointmentStatus.IN_PROGRESS 
        }
    }
    
    fun getPatientsByStatus(status: PatientStatus): List<AssignedPatient> {
        return MOCK_ASSIGNED_PATIENTS.filter { it.status == status }
    }
    
    fun getCriticalPatients(): List<AssignedPatient> {
        return MOCK_ASSIGNED_PATIENTS.filter { it.status == PatientStatus.CRITICAL || it.emergencyFlag }
    }
    
    fun getRecordsByPatient(patientId: String): List<PatientRecord> {
        return MOCK_PATIENT_RECORDS.filter { it.patientId == patientId }
    }
    
    fun getRecentRecords(limit: Int = 10): List<PatientRecord> {
        return MOCK_PATIENT_RECORDS.take(limit)
    }
}
