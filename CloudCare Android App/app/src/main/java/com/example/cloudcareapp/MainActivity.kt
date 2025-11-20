package com.example.cloudcareapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import com.example.cloudcareapp.navigation.*
import com.example.cloudcareapp.ui.screens.about.AboutScreen
import com.example.cloudcareapp.ui.screens.auth.*
import com.example.cloudcareapp.ui.screens.consents.ConsentsScreen
import com.example.cloudcareapp.ui.screens.patient.ConsentRequestsScreen
import com.example.cloudcareapp.ui.screens.dashboard.DashboardScreen
import com.example.cloudcareapp.ui.screens.doctor.DoctorDashboardScreen
import com.example.cloudcareapp.ui.screens.doctor.DoctorEmergencyScreen
import com.example.cloudcareapp.ui.screens.doctor.DoctorNotificationsScreen
import com.example.cloudcareapp.ui.screens.doctor.DoctorPatientsScreen
import com.example.cloudcareapp.ui.screens.doctor.DoctorProfileScreen
import com.example.cloudcareapp.ui.screens.doctor.DoctorQRScannerScreen
import com.example.cloudcareapp.ui.screens.doctor.DoctorRecordsScreen
import com.example.cloudcareapp.ui.screens.doctor.DoctorScheduleScreen
import com.example.cloudcareapp.ui.screens.records.DocumentUploadScreen
import com.example.cloudcareapp.ui.screens.facilities.FacilitiesScreen
import com.example.cloudcareapp.ui.screens.help.HelpSupportScreen
import com.example.cloudcareapp.ui.screens.hospital.HospitalAdmissionsScreen
import com.example.cloudcareapp.ui.screens.hospital.HospitalDashboardScreen
import com.example.cloudcareapp.ui.screens.hospital.HospitalMainScreen
import com.example.cloudcareapp.ui.screens.hospital.HospitalProfileScreen
import com.example.cloudcareapp.ui.screens.hospital.HospitalResourcesScreen
import com.example.cloudcareapp.ui.screens.hospital.HospitalStaffScreen
import com.example.cloudcareapp.ui.screens.notifications.NotificationsScreen
import com.example.cloudcareapp.ui.screens.privacy.PrivacySecurityScreen
import com.example.cloudcareapp.ui.screens.profile.EditProfileScreen
import com.example.cloudcareapp.ui.screens.profile.PatientProfileScreen
import com.example.cloudcareapp.ui.screens.profile.ProfileScreen
import com.example.cloudcareapp.ui.screens.records.RecordsScreen
import com.example.cloudcareapp.ui.screens.scanshare.ScanShareScreen
import com.example.cloudcareapp.ui.screens.settings.SettingsScreen
import com.example.cloudcareapp.ui.screens.splash.SplashScreen
import com.example.cloudcareapp.ui.screens.wearables.WearablesScreen
import com.example.cloudcareapp.ui.theme.CloudCareAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CloudCareAppTheme {
                CloudCareApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudCareApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current
    
    // Drawer state for menu
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Determine if we should show bottom nav and top bar
    val showBottomNav = currentDestination?.route in bottomNavItems.map { it.route }
    val showDoctorBottomNav = currentDestination?.route in doctorBottomNavItems.map { it.route }
    val showHospitalBottomNav = currentDestination?.route in hospitalBottomNavItems.map { it.route }
    val showTopBar = showBottomNav || showDoctorBottomNav || showHospitalBottomNav
    
    // Logout function
    val handleLogout = {
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
        navController.navigate(Screen.LoginSelection.route) {
            popUpTo(0) { inclusive = true }
        }
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "CloudCare Menu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        label = { Text("Settings") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.Settings.route)
                        }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Help, contentDescription = null) },
                        label = { Text("Help & Support") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.HelpSupport.route)
                        }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                        label = { Text("About") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.About.route)
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Logout, contentDescription = null) },
                        label = { Text("Logout") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            handleLogout()
                        }
                    )
                }
            }
        }
    ) {
    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentDestination?.route) {
                                Screen.Dashboard.route -> "CloudCare"
                                Screen.Wearables.route -> "Wearables & Devices"
                                Screen.Records.route -> "My Records"
                                Screen.ScanShare.route -> "Scan & Share"
                                Screen.Consents.route -> "Consents"
                                Screen.DoctorDashboard.route -> "Doctor Dashboard"
                                Screen.DoctorPatients.route -> "My Patients"
                                Screen.DoctorSchedule.route -> "Appointments"
                                Screen.HospitalDashboard.route -> "Hospital Dashboard"
                                Screen.HospitalAdmissions.route -> "Patients"
                                Screen.HospitalStaff.route -> "Hospital Staff"
                                else -> "CloudCare"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            scope.launch { 
                                if (drawerState.isOpen) drawerState.close()
                                else drawerState.open()
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        if (showDoctorBottomNav) {
                            IconButton(onClick = { 
                                navController.navigate(Screen.DoctorQRScanner.route)
                            }) {
                                Icon(Icons.Filled.QrCodeScanner, contentDescription = "QR Scanner")
                            }
                            IconButton(onClick = { 
                                navController.navigate(Screen.DoctorNotifications.route)
                            }) {
                                Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                            }
                            IconButton(onClick = { 
                                navController.navigate(Screen.DoctorProfile.route)
                            }) {
                                Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
                            }
                        } else if (showHospitalBottomNav) {
                            IconButton(onClick = { 
                                navController.navigate(Screen.Notifications.route)
                            }) {
                                Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                            }
                            IconButton(onClick = { 
                                navController.navigate(Screen.HospitalProfile.route)
                            }) {
                                Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
                            }
                        } else {
                            IconButton(onClick = { 
                                navController.navigate(Screen.ScanShare.route)
                            }) {
                                Icon(Icons.Filled.QrCodeScanner, contentDescription = "QR Scanner")
                            }
                            BadgedBox(
                                badge = {
                                    Badge { Text("18") }
                                }
                            ) {
                                IconButton(onClick = { 
                                    navController.navigate(Screen.Notifications.route)
                                }) {
                                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                                }
                            }
                            IconButton(onClick = { 
                                navController.navigate(Screen.Profile.route)
                            }) {
                                Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomNav) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = getIconForRoute(screen.route, selected),
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            } else if (showDoctorBottomNav) {
                NavigationBar {
                    doctorBottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = getIconForRoute(screen.route, selected),
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            } else if (showHospitalBottomNav) {
                NavigationBar {
                    hospitalBottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = getIconForRoute(screen.route, selected),
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Splash Screen
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToLoginSelection = {
                        navController.navigate(Screen.LoginSelection.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            
            // Login Selection Screen
            composable(Screen.LoginSelection.route) {
                LoginSelectionScreen(
                    onPatientLoginClick = {
                        navController.navigate(Screen.PatientLogin.route)
                    },
                    onDoctorLoginClick = {
                        navController.navigate(Screen.DoctorLogin.route)
                    },
                    onHospitalLoginClick = {
                        navController.navigate(Screen.HospitalLogin.route)
                    }
                )
            }
            
            // Patient Login
            composable(Screen.PatientLogin.route) {
                PatientLoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.LoginSelection.route) { inclusive = true }
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSignupClick = {
                        navController.navigate(Screen.PatientSignup.route)
                    }
                )
            }
            
            // Patient Signup
            composable(Screen.PatientSignup.route) {
                PatientSignupScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSignupSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.LoginSelection.route) { inclusive = true }
                        }
                    }
                )
            }
            
            // Doctor Login
            composable(Screen.DoctorLogin.route) {
                DoctorLoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.DoctorDashboard.route) {
                            popUpTo(Screen.LoginSelection.route) { inclusive = true }
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSignupClick = {
                        navController.navigate(Screen.DoctorSignup.route)
                    }
                )
            }
            
            // Doctor Signup
            composable(Screen.DoctorSignup.route) {
                DoctorSignupScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSignupSuccess = {
                        navController.navigate(Screen.DoctorDashboard.route) {
                            popUpTo(Screen.LoginSelection.route) { inclusive = true }
                        }
                    }
                )
            }
            
            // Hospital Login
            composable(Screen.HospitalLogin.route) {
                HospitalLoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.HospitalDashboard.route) {
                            popUpTo(Screen.LoginSelection.route) { inclusive = true }
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSignupClick = {
                        navController.navigate(Screen.HospitalSignup.route)
                    }
                )
            }
            
            // Hospital Signup
            composable(Screen.HospitalSignup.route) {
                HospitalSignupScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSignupSuccess = {
                        navController.navigate(Screen.HospitalDashboard.route) {
                            popUpTo(Screen.LoginSelection.route) { inclusive = true }
                        }
                    }
                )
            }
            
            // Patient Dashboard
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToWearables = {
                        navController.navigate(Screen.Wearables.route)
                    },
                    onNavigateToRecords = {
                        navController.navigate(Screen.Records.route)
                    },
                    onNavigateToFacilities = {
                        navController.navigate(Screen.LinkedFacilities.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToConsents = {
                        navController.navigate(Screen.Consents.route)
                    },
                    onNavigateToDevices = {
                        navController.navigate(Screen.Wearables.route)
                    }
                )
            }
            
            // Doctor Dashboard
            composable(Screen.DoctorDashboard.route) {
                DoctorDashboardScreen(
                    onLogout = handleLogout,
                    onMenuClick = {
                        scope.launch { 
                            if (drawerState.isOpen) drawerState.close()
                            else drawerState.open()
                        }
                    },
                    onNavigateToPatients = {
                        navController.navigate(Screen.DoctorPatients.route)
                    },
                    onNavigateToEmergency = {
                        navController.navigate(Screen.DoctorEmergency.route)
                    },
                    onNavigateToSchedule = {
                        navController.navigate(Screen.DoctorSchedule.route)
                    },
                    onNavigateToRecords = {
                        navController.navigate(Screen.DoctorRecords.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.DoctorProfile.route)
                    },
                    onNavigateToNotifications = {
                        navController.navigate(Screen.DoctorNotifications.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToQRScanner = {
                        navController.navigate(Screen.DoctorQRScanner.route)
                    }
                )
            }
            
            // Hospital Dashboard
            composable(Screen.HospitalDashboard.route) {
                HospitalDashboardScreen(
                    onLogout = handleLogout,
                    onMenuClick = {
                        scope.launch { 
                            if (drawerState.isOpen) drawerState.close()
                            else drawerState.open()
                        }
                    },
                    onNotificationClick = {
                        navController.navigate(Screen.Notifications.route)
                    },
                    onNavigateToStaff = {
                        navController.navigate(Screen.HospitalStaff.route)
                    },
                    onNavigateToResources = {
                        navController.navigate(Screen.HospitalResources.route)
                    },
                    onNavigateToAdmissions = {
                        navController.navigate(Screen.HospitalAdmissions.route)
                    },
                    onProfileClick = {
                        navController.navigate(Screen.HospitalProfile.route)
                    }
                )
            }
            
            composable(Screen.Wearables.route) {
                WearablesScreen(
                    navController = navController
                )
            }
            
            composable(Screen.Records.route) {
                RecordsScreen(
                    onNavigateToUpload = {
                        navController.navigate(Screen.DocumentUpload.route)
                    }
                )
            }
            
            composable(Screen.ScanShare.route) {
                ScanShareScreen()
            }
            
            composable(Screen.Consents.route) {
                ConsentRequestsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.LinkedFacilities.route) {
                FacilitiesScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.Profile.route) {
                // ✅ FIX: Use PatientProfileScreen with real data instead of mock ProfileScreen
                PatientProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveSuccess = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            // Doctor Detail Screens
            composable(Screen.DoctorPatients.route) {
                DoctorPatientsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.DoctorEmergency.route) {
                DoctorEmergencyScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.DoctorSchedule.route) {
                DoctorScheduleScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.DoctorRecords.route) {
                DoctorRecordsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.DoctorProfile.route) {
                DoctorProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.DoctorNotifications.route) {
                DoctorNotificationsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.DoctorQRScanner.route) {
                DoctorQRScannerScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            // Hospital Detail Screens
            composable(Screen.HospitalStaff.route) {
                HospitalStaffScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.HospitalResources.route) {
                HospitalResourcesScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.HospitalAdmissions.route) {
                com.example.cloudcareapp.ui.screens.hospital.HospitalPatientsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onPatientClick = { patientId ->
                        // Navigate to patient details
                        // We need a route for patient details. 
                        // HospitalMainScreen used "patient_details/{patientId}"
                        // We should probably add this route to Screen.kt or use a generic one.
                        // For now, let's assume we can navigate to a new route.
                        navController.navigate("hospital_patient_details/$patientId")
                    }
                )
            }
            
            composable(
                route = "hospital_patient_details/{patientId}",
                arguments = listOf(androidx.navigation.navArgument("patientId") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val patientId = backStackEntry.arguments?.getString("patientId") ?: return@composable
                com.example.cloudcareapp.ui.screens.hospital.HospitalPatientDetailsScreen(
                    patientId = patientId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onViewRecords = { pid ->
                        navController.navigate("records/$pid")
                    }
                )
            }
            
            composable(
                route = "records/{patientId}",
                arguments = listOf(androidx.navigation.navArgument("patientId") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val patientId = backStackEntry.arguments?.getString("patientId")
                RecordsScreen(
                    patientId = patientId,
                    onNavigateToUpload = {
                        // Upload disabled in viewer mode
                    }
                )
            }
            
            composable(Screen.HospitalProfile.route) {
                HospitalProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            // Common Screens
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.HelpSupport.route) {
                HelpSupportScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.About.route) {
                AboutScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.PrivacySecurity.route) {
                PrivacySecurityScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.DocumentUpload.route) {
                DocumentUploadScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
    }
}

fun getIconForRoute(route: String, selected: Boolean): ImageVector {
    return when (route) {
        Screen.Dashboard.route -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
        Screen.Records.route -> if (selected) Icons.Filled.Description else Icons.Outlined.Description
        Screen.ScanShare.route -> if (selected) Icons.Filled.QrCodeScanner else Icons.Outlined.QrCodeScanner
        Screen.Consents.route -> if (selected) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle
        
        // Doctor Icons
        Screen.DoctorDashboard.route -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
        Screen.DoctorPatients.route -> if (selected) Icons.Filled.People else Icons.Outlined.People
        Screen.DoctorSchedule.route -> if (selected) Icons.Filled.CalendarToday else Icons.Outlined.CalendarToday
        
        // Hospital Icons
        Screen.HospitalDashboard.route -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
        Screen.HospitalAdmissions.route -> if (selected) Icons.Filled.People else Icons.Outlined.People
        Screen.HospitalStaff.route -> if (selected) Icons.Filled.Badge else Icons.Outlined.Badge
        
        else -> Icons.Filled.Home
    }
}