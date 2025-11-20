package com.example.cloudcareapp.ui.screens.hospital

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cloudcareapp.ui.theme.Success

@Composable
fun HospitalMainScreen(
    onLogout: () -> Unit,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onNavigateToStaff: () -> Unit,
    onNavigateToResources: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Success
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = selectedItem == 0,
                    onClick = {
                        selectedItem = 0
                        navController.navigate("dashboard") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Success,
                        selectedTextColor = Success,
                        indicatorColor = Success.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.People, contentDescription = "Patients") },
                    label = { Text("Patients") },
                    selected = selectedItem == 1,
                    onClick = {
                        selectedItem = 1
                        navController.navigate("patients") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Success,
                        selectedTextColor = Success,
                        indicatorColor = Success.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = selectedItem == 2,
                    onClick = {
                        selectedItem = 2
                        onNavigateToProfile()
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Success,
                        selectedTextColor = Success,
                        indicatorColor = Success.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(navController = navController, startDestination = "dashboard") {
                composable("dashboard") {
                    HospitalDashboardScreen(
                        onLogout = onLogout,
                        onMenuClick = onMenuClick,
                        onNotificationClick = onNotificationClick,
                        onNavigateToAdmissions = {
                            selectedItem = 1
                            navController.navigate("patients")
                        },
                        onNavigateToResources = onNavigateToResources,
                        onNavigateToStaff = onNavigateToStaff,
                        onProfileClick = onNavigateToProfile
                    )
                }
                composable("patients") {
                    HospitalPatientsScreen(
                        onBackClick = {
                            selectedItem = 0
                            navController.navigate("dashboard")
                        },
                        onPatientClick = { patientId ->
                            navController.navigate("patient_details/$patientId")
                        }
                    )
                }
                composable(
                    route = "patient_details/{patientId}",
                    arguments = listOf(navArgument("patientId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val patientId = backStackEntry.arguments?.getString("patientId") ?: return@composable
                    HospitalPatientDetailsScreen(
                        patientId = patientId,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onViewRecords = { pid ->
                            // Navigate to records screen for this patient
                            navController.navigate("records/$pid")
                        }
                    )
                }
            }
        }
    }
}
