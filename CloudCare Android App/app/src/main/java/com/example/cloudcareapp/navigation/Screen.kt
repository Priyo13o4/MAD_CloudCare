package com.example.cloudcareapp.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object LoginSelection : Screen("login_selection")
    object PatientLogin : Screen("patient_login")
    object DoctorLogin : Screen("doctor_login")
    object HospitalLogin : Screen("hospital_login")
    
    // Patient Screens
    object Dashboard : Screen("dashboard")
    object Wearables : Screen("wearables")
    object Records : Screen("records")
    object ScanShare : Screen("scan_share")
    object Consents : Screen("consents")
    object LinkedFacilities : Screen("linked_facilities")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
    object EditProfile : Screen("edit_profile")
    
    // Doctor Screens
    object DoctorDashboard : Screen("doctor_dashboard")
    object DoctorPatients : Screen("doctor_patients")
    object DoctorEmergency : Screen("doctor_emergency")
    object DoctorSchedule : Screen("doctor_schedule")
    object DoctorRecords : Screen("doctor_records")
    
    // Hospital Screens
    object HospitalDashboard : Screen("hospital_dashboard")
    object HospitalStaff : Screen("hospital_staff")
    object HospitalResources : Screen("hospital_resources")
    object HospitalAdmissions : Screen("hospital_admissions")
    
    // Common Screens
    object Settings : Screen("settings")
    object HelpSupport : Screen("help_support")
    object About : Screen("about")
    object PrivacySecurity : Screen("privacy_security")
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: String // We'll use icon name strings
) {
    object Dashboard : BottomNavItem(
        route = Screen.Dashboard.route,
        title = "Dashboard",
        icon = "home"
    )
    
    object Records : BottomNavItem(
        route = Screen.Records.route,
        title = "My Records",
        icon = "description"
    )
    
    object ScanShare : BottomNavItem(
        route = Screen.ScanShare.route,
        title = "Scan & Share",
        icon = "qr_code_scanner"
    )
    
    object Consents : BottomNavItem(
        route = Screen.Consents.route,
        title = "Consents",
        icon = "check_circle"
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Dashboard,
    BottomNavItem.Records,
    BottomNavItem.ScanShare,
    BottomNavItem.Consents
)
