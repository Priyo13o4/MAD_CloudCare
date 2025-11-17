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
import com.example.cloudcareapp.ui.screens.dashboard.DashboardScreen
import com.example.cloudcareapp.ui.screens.doctor.DoctorDashboardScreen
import com.example.cloudcareapp.ui.screens.doctor.DoctorEmergencyScreen
import com.example.cloudcareapp.ui.screens.doctor.DoctorPatientsScreen
import com.example.cloudcareapp.ui.screens.doctor.DoctorRecordsScreen
import com.example.cloudcareapp.ui.screens.doctor.DoctorScheduleScreen
import com.example.cloudcareapp.ui.screens.facilities.FacilitiesScreen
import com.example.cloudcareapp.ui.screens.help.HelpSupportScreen
import com.example.cloudcareapp.ui.screens.hospital.HospitalAdmissionsScreen
import com.example.cloudcareapp.ui.screens.hospital.HospitalDashboardScreen
import com.example.cloudcareapp.ui.screens.hospital.HospitalResourcesScreen
import com.example.cloudcareapp.ui.screens.hospital.HospitalStaffScreen
import com.example.cloudcareapp.ui.screens.notifications.NotificationsScreen
import com.example.cloudcareapp.ui.screens.privacy.PrivacySecurityScreen
import com.example.cloudcareapp.ui.screens.profile.EditProfileScreen
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
    val showTopBar = showBottomNav
    
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
                    }
                )
            }
            
            // Hospital Dashboard
            composable(Screen.HospitalDashboard.route) {
                HospitalDashboardScreen(
                    onLogout = handleLogout,
                    onNavigateToStaff = {
                        navController.navigate(Screen.HospitalStaff.route)
                    },
                    onNavigateToResources = {
                        navController.navigate(Screen.HospitalResources.route)
                    },
                    onNavigateToAdmissions = {
                        navController.navigate(Screen.HospitalAdmissions.route)
                    }
                )
            }
            
            composable(Screen.Wearables.route) {
                WearablesScreen(
                    navController = navController
                )
            }
            
            composable(Screen.Records.route) {
                RecordsScreen()
            }
            
            composable(Screen.ScanShare.route) {
                ScanShareScreen()
            }
            
            composable(Screen.Consents.route) {
                ConsentsScreen()
            }
            
            composable(Screen.LinkedFacilities.route) {
                FacilitiesScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onEditProfileClick = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onLogout = handleLogout,
                    onNavigateToNotifications = {
                        navController.navigate(Screen.Notifications.route)
                    },
                    onNavigateToPrivacy = {
                        navController.navigate(Screen.PrivacySecurity.route)
                    },
                    onNavigateToHelp = {
                        navController.navigate(Screen.HelpSupport.route)
                    },
                    onNavigateToAbout = {
                        navController.navigate(Screen.About.route)
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
                HospitalAdmissionsScreen(
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
        else -> Icons.Filled.Home
    }
}