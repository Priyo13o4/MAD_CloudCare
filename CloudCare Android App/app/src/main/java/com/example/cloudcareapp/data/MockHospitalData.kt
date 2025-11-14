package com.example.cloudcareapp.data

import com.example.cloudcareapp.data.model.*

object MockHospitalData {
    
    val MOCK_HOSPITAL_STATS = HospitalStats(
        admittedPatients = 284,
        availableDoctors = 48,
        emergencyCases = 12,
        avgResponseTime = "8 min",
        totalBeds = 284,
        availableBeds = 62
    )
    
    val MOCK_EMERGENCY_CASES = listOf(
        EmergencyCase(
            id = "E-001",
            patientId = "P-001",
            patientName = "John Anderson",
            age = 65,
            gender = "M",
            condition = "Cardiac Arrest",
            severity = EmergencySeverity.CRITICAL,
            status = EmergencyStatus.IN_TREATMENT,
            admittedTime = "15 mins ago",
            assignedDoctor = "Dr. Sarah Mitchell",
            department = "Cardiology"
        ),
        EmergencyCase(
            id = "E-002",
            patientId = "P-002",
            patientName = "Maria Garcia",
            age = 42,
            gender = "F",
            condition = "Severe Trauma",
            severity = EmergencySeverity.HIGH,
            status = EmergencyStatus.STABLE,
            admittedTime = "45 mins ago",
            assignedDoctor = "Dr. Michael Chen",
            department = "Emergency"
        ),
        EmergencyCase(
            id = "E-003",
            patientId = "P-003",
            patientName = "Robert Williams",
            age = 58,
            gender = "M",
            condition = "Stroke",
            severity = EmergencySeverity.CRITICAL,
            status = EmergencyStatus.IN_TREATMENT,
            admittedTime = "1 hour ago",
            assignedDoctor = "Dr. Emily Johnson",
            department = "Neurology"
        ),
        EmergencyCase(
            id = "E-004",
            patientId = "P-004",
            patientName = "Lisa Thompson",
            age = 35,
            gender = "F",
            condition = "Severe Allergic Reaction",
            severity = EmergencySeverity.HIGH,
            status = EmergencyStatus.STABLE,
            admittedTime = "2 hours ago",
            assignedDoctor = "Dr. David Lee",
            department = "Emergency"
        ),
        EmergencyCase(
            id = "E-005",
            patientId = "P-005",
            patientName = "James Martin",
            age = 71,
            gender = "M",
            condition = "Respiratory Failure",
            severity = EmergencySeverity.CRITICAL,
            status = EmergencyStatus.IN_TREATMENT,
            admittedTime = "3 hours ago",
            assignedDoctor = "Dr. Sarah Wilson",
            department = "Pulmonology"
        ),
        EmergencyCase(
            id = "E-006",
            patientId = "P-006",
            patientName = "Patricia Brown",
            age = 48,
            gender = "F",
            condition = "Acute Appendicitis",
            severity = EmergencySeverity.MEDIUM,
            status = EmergencyStatus.WAITING,
            admittedTime = "4 hours ago",
            assignedDoctor = "Dr. John Davis",
            department = "Surgery"
        ),
        EmergencyCase(
            id = "E-007",
            patientId = "P-007",
            patientName = "Christopher Lee",
            age = 52,
            gender = "M",
            condition = "Chest Pain",
            severity = EmergencySeverity.MEDIUM,
            status = EmergencyStatus.STABLE,
            admittedTime = "5 hours ago",
            assignedDoctor = "Dr. Sarah Mitchell",
            department = "Cardiology"
        ),
        EmergencyCase(
            id = "E-008",
            patientId = "P-008",
            patientName = "Nancy White",
            age = 67,
            gender = "F",
            condition = "Diabetic Emergency",
            severity = EmergencySeverity.HIGH,
            status = EmergencyStatus.IN_TREATMENT,
            admittedTime = "6 hours ago",
            assignedDoctor = "Dr. Michael Chen",
            department = "Endocrinology"
        ),
        EmergencyCase(
            id = "E-009",
            patientId = "P-009",
            patientName = "Daniel Harris",
            age = 29,
            gender = "M",
            condition = "Fracture - Multiple",
            severity = EmergencySeverity.MEDIUM,
            status = EmergencyStatus.STABLE,
            admittedTime = "7 hours ago",
            assignedDoctor = "Dr. Emily Johnson",
            department = "Orthopedics"
        ),
        EmergencyCase(
            id = "E-010",
            patientId = "P-010",
            patientName = "Karen Martinez",
            age = 55,
            gender = "F",
            condition = "Severe Dehydration",
            severity = EmergencySeverity.LOW,
            status = EmergencyStatus.STABLE,
            admittedTime = "8 hours ago",
            assignedDoctor = "Dr. David Lee",
            department = "Emergency"
        ),
        EmergencyCase(
            id = "E-011",
            patientId = "P-011",
            patientName = "Steven Clark",
            age = 61,
            gender = "M",
            condition = "Heart Attack",
            severity = EmergencySeverity.CRITICAL,
            status = EmergencyStatus.DISCHARGED,
            admittedTime = "12 hours ago",
            assignedDoctor = "Dr. Sarah Mitchell",
            department = "Cardiology"
        ),
        EmergencyCase(
            id = "E-012",
            patientId = "P-012",
            patientName = "Michelle Rodriguez",
            age = 38,
            gender = "F",
            condition = "Asthma Attack",
            severity = EmergencySeverity.MEDIUM,
            status = EmergencyStatus.DISCHARGED,
            admittedTime = "14 hours ago",
            assignedDoctor = "Dr. Sarah Wilson",
            department = "Pulmonology"
        )
    )
    
    val MOCK_HOSPITAL_STAFF = listOf(
        HospitalStaff(
            id = "S-001",
            name = "Dr. Sarah Mitchell",
            age = 42,
            specialization = "Cardiology",
            department = "Cardiology",
            patientCount = 18,
            status = StaffStatus.ACTIVE,
            joinDate = "Mar 2015",
            email = "s.mitchell@hospital.org",
            phone = "+1-555-0101"
        ),
        HospitalStaff(
            id = "S-002",
            name = "Dr. Michael Chen",
            age = 38,
            specialization = "Emergency Medicine",
            department = "Emergency",
            patientCount = 22,
            status = StaffStatus.ACTIVE,
            joinDate = "Aug 2017",
            email = "m.chen@hospital.org",
            phone = "+1-555-0102"
        ),
        HospitalStaff(
            id = "S-003",
            name = "Dr. Emily Johnson",
            age = 35,
            specialization = "Neurology",
            department = "Neurology",
            patientCount = 15,
            status = StaffStatus.ACTIVE,
            joinDate = "Jan 2018",
            email = "e.johnson@hospital.org",
            phone = "+1-555-0103"
        ),
        HospitalStaff(
            id = "S-004",
            name = "Dr. David Lee",
            age = 45,
            specialization = "Emergency Medicine",
            department = "Emergency",
            patientCount = 20,
            status = StaffStatus.ACTIVE,
            joinDate = "May 2014",
            email = "d.lee@hospital.org",
            phone = "+1-555-0104"
        ),
        HospitalStaff(
            id = "S-005",
            name = "Dr. Sarah Wilson",
            age = 40,
            specialization = "Pulmonology",
            department = "Pulmonology",
            patientCount = 16,
            status = StaffStatus.ACTIVE,
            joinDate = "Sep 2016",
            email = "s.wilson@hospital.org",
            phone = "+1-555-0105"
        ),
        HospitalStaff(
            id = "S-006",
            name = "Dr. John Davis",
            age = 50,
            specialization = "General Surgery",
            department = "Surgery",
            patientCount = 12,
            status = StaffStatus.ON_LEAVE,
            joinDate = "Feb 2012",
            email = "j.davis@hospital.org",
            phone = "+1-555-0106"
        ),
        HospitalStaff(
            id = "S-007",
            name = "Dr. Lisa Anderson",
            age = 36,
            specialization = "Pediatrics",
            department = "Pediatrics",
            patientCount = 25,
            status = StaffStatus.ACTIVE,
            joinDate = "Nov 2018",
            email = "l.anderson@hospital.org",
            phone = "+1-555-0107"
        ),
        HospitalStaff(
            id = "S-008",
            name = "Dr. Robert Taylor",
            age = 48,
            specialization = "Orthopedics",
            department = "Orthopedics",
            patientCount = 14,
            status = StaffStatus.ACTIVE,
            joinDate = "Apr 2015",
            email = "r.taylor@hospital.org",
            phone = "+1-555-0108"
        ),
        HospitalStaff(
            id = "S-009",
            name = "Dr. Jennifer White",
            age = 33,
            specialization = "Endocrinology",
            department = "Endocrinology",
            patientCount = 17,
            status = StaffStatus.ACTIVE,
            joinDate = "Jul 2019",
            email = "j.white@hospital.org",
            phone = "+1-555-0109"
        ),
        HospitalStaff(
            id = "S-010",
            name = "Dr. William Brown",
            age = 52,
            specialization = "Oncology",
            department = "Oncology",
            patientCount = 10,
            status = StaffStatus.ACTIVE,
            joinDate = "Jun 2013",
            email = "w.brown@hospital.org",
            phone = "+1-555-0110"
        ),
        HospitalStaff(
            id = "S-011",
            name = "Nurse Emma Clark",
            age = 28,
            specialization = "ICU Nursing",
            department = "ICU",
            patientCount = 8,
            status = StaffStatus.ACTIVE,
            joinDate = "Jan 2020",
            email = "e.clark@hospital.org",
            phone = "+1-555-0111"
        ),
        HospitalStaff(
            id = "S-012",
            name = "Nurse James Garcia",
            age = 31,
            specialization = "Emergency Nursing",
            department = "Emergency",
            patientCount = 12,
            status = StaffStatus.ACTIVE,
            joinDate = "Mar 2019",
            email = "j.garcia@hospital.org",
            phone = "+1-555-0112"
        ),
        HospitalStaff(
            id = "S-013",
            name = "Nurse Olivia Martinez",
            age = 26,
            specialization = "Pediatric Nursing",
            department = "Pediatrics",
            patientCount = 15,
            status = StaffStatus.ACTIVE,
            joinDate = "Sep 2021",
            email = "o.martinez@hospital.org",
            phone = "+1-555-0113"
        ),
        HospitalStaff(
            id = "S-014",
            name = "Dr. Thomas Rodriguez",
            age = 44,
            specialization = "Radiology",
            department = "Radiology",
            patientCount = 0,
            status = StaffStatus.ACTIVE,
            joinDate = "Oct 2016",
            email = "t.rodriguez@hospital.org",
            phone = "+1-555-0114"
        ),
        HospitalStaff(
            id = "S-015",
            name = "Dr. Amanda Lewis",
            age = 39,
            specialization = "Anesthesiology",
            department = "Surgery",
            patientCount = 0,
            status = StaffStatus.UNAVAILABLE,
            joinDate = "Dec 2017",
            email = "a.lewis@hospital.org",
            phone = "+1-555-0115"
        )
    )
    
    val MOCK_DEPARTMENTS = listOf(
        Department(
            id = "D-001",
            name = "Cardiology",
            totalBeds = 45,
            occupiedBeds = 38,
            headDoctor = "Dr. Sarah Mitchell",
            status = DepartmentStatus.BUSY
        ),
        Department(
            id = "D-002",
            name = "Emergency",
            totalBeds = 30,
            occupiedBeds = 28,
            headDoctor = "Dr. Michael Chen",
            status = DepartmentStatus.CRITICAL
        ),
        Department(
            id = "D-003",
            name = "Neurology",
            totalBeds = 35,
            occupiedBeds = 22,
            headDoctor = "Dr. Emily Johnson",
            status = DepartmentStatus.NORMAL
        ),
        Department(
            id = "D-004",
            name = "Pediatrics",
            totalBeds = 50,
            occupiedBeds = 35,
            headDoctor = "Dr. Lisa Anderson",
            status = DepartmentStatus.NORMAL
        ),
        Department(
            id = "D-005",
            name = "Surgery",
            totalBeds = 40,
            occupiedBeds = 32,
            headDoctor = "Dr. John Davis",
            status = DepartmentStatus.BUSY
        ),
        Department(
            id = "D-006",
            name = "ICU",
            totalBeds = 25,
            occupiedBeds = 23,
            headDoctor = "Dr. David Lee",
            status = DepartmentStatus.CRITICAL
        )
    )
    
    val MOCK_HOSPITAL_RESOURCES = listOf(
        HospitalResource(
            id = "R-001",
            name = "General Beds",
            category = ResourceCategory.BEDS,
            total = 200,
            available = 45,
            inUse = 155,
            status = ResourceStatus.LOW
        ),
        HospitalResource(
            id = "R-002",
            name = "ICU Beds",
            category = ResourceCategory.BEDS,
            total = 50,
            available = 8,
            inUse = 42,
            status = ResourceStatus.CRITICAL
        ),
        HospitalResource(
            id = "R-003",
            name = "Ventilators",
            category = ResourceCategory.EQUIPMENT,
            total = 50,
            available = 5,
            inUse = 45,
            status = ResourceStatus.CRITICAL
        ),
        HospitalResource(
            id = "R-004",
            name = "Oxygen Cylinders",
            category = ResourceCategory.SUPPLIES,
            total = 150,
            available = 35,
            inUse = 115,
            status = ResourceStatus.LOW
        ),
        HospitalResource(
            id = "R-005",
            name = "ECG Machines",
            category = ResourceCategory.EQUIPMENT,
            total = 35,
            available = 12,
            inUse = 23,
            status = ResourceStatus.NORMAL
        ),
        HospitalResource(
            id = "R-006",
            name = "Wheelchairs",
            category = ResourceCategory.EQUIPMENT,
            total = 80,
            available = 28,
            inUse = 52,
            status = ResourceStatus.NORMAL
        ),
        HospitalResource(
            id = "R-007",
            name = "Blood Units (O-)",
            category = ResourceCategory.SUPPLIES,
            total = 200,
            available = 45,
            inUse = 155,
            status = ResourceStatus.LOW
        ),
        HospitalResource(
            id = "R-008",
            name = "Surgical Kits",
            category = ResourceCategory.SUPPLIES,
            total = 40,
            available = 15,
            inUse = 25,
            status = ResourceStatus.NORMAL
        ),
        HospitalResource(
            id = "R-009",
            name = "Defibrillators",
            category = ResourceCategory.EQUIPMENT,
            total = 25,
            available = 8,
            inUse = 17,
            status = ResourceStatus.NORMAL
        ),
        HospitalResource(
            id = "R-010",
            name = "PPE Kits",
            category = ResourceCategory.SUPPLIES,
            total = 500,
            available = 120,
            inUse = 380,
            status = ResourceStatus.LOW
        )
    )
    
    fun getCriticalEmergencies(): List<EmergencyCase> {
        return MOCK_EMERGENCY_CASES.filter { it.severity == EmergencySeverity.CRITICAL }
    }
    
    fun getActiveEmergencies(): List<EmergencyCase> {
        return MOCK_EMERGENCY_CASES.filter { 
            it.status != EmergencyStatus.DISCHARGED 
        }
    }
    
    fun getActiveStaff(): List<HospitalStaff> {
        return MOCK_HOSPITAL_STAFF.filter { it.status == StaffStatus.ACTIVE }
    }
    
    fun getCriticalResources(): List<HospitalResource> {
        return MOCK_HOSPITAL_RESOURCES.filter { it.status == ResourceStatus.CRITICAL }
    }
    
    fun getResourcesByCategory(category: ResourceCategory): List<HospitalResource> {
        return MOCK_HOSPITAL_RESOURCES.filter { it.category == category }
    }
}
