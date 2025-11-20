package com.example.cloudcareapp.ui.screens.doctor

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.PatientQRData
import com.example.cloudcareapp.data.remote.RetrofitClient
import com.example.cloudcareapp.ui.components.CommonTopAppBar
import com.example.cloudcareapp.ui.theme.*
import com.example.cloudcareapp.ui.viewmodel.DoctorProfileViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardScreen(
    onLogout: () -> Unit,
    onMenuClick: (() -> Unit)? = null,
    onNavigateToPatients: () -> Unit = {},
    onNavigateToEmergency: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToRecords: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToQRScanner: () -> Unit = {}
) {
    val viewModel: DoctorProfileViewModel = viewModel()
    val doctorProfile by viewModel.doctorProfile.observeAsState()
    val patients by viewModel.patients.observeAsState(emptyList())
    
    LaunchedEffect(Unit) {
        AppDataCache.getDoctorId()?.let { doctorId ->
            viewModel.refresh(doctorId)
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DoctorBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Welcome Card with Professional Gradient
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(DoctorPrimary, DoctorAccent)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MedicalServices,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Welcome, Dr. ${doctorProfile?.firstName?.split(" ")?.firstOrNull() ?: "Doctor"}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = doctorProfile?.specialization ?: "Specialist",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Text(
                text = "Today's Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DoctorTextPrimary
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DoctorStatCard(
                    title = "Patients",
                    value = patients.size.toString(),
                    icon = Icons.Filled.People,
                    color = DoctorPrimary,
                    backgroundColor = DoctorCardTeal,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToPatients
                )
                DoctorStatCard(
                    title = "Appointments",
                    value = "8", // TODO: Fetch appointments count
                    icon = Icons.Filled.CalendarToday,
                    color = DoctorAccent,
                    backgroundColor = DoctorCardBlue,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSchedule
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DoctorStatCard(
                    title = "Emergency",
                    value = patients.count { it.emergencyFlag }.toString(),
                    icon = Icons.Filled.Warning,
                    color = DoctorError,
                    backgroundColor = Color(0xFFFEE2E2),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToEmergency
                )
                DoctorStatCard(
                    title = "Records",
                    value = "45", // TODO: Fetch records count
                    icon = Icons.Filled.Description,
                    color = DoctorSuccess,
                    backgroundColor = Color(0xFFD1FAE5),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToRecords
                )
            }
        }
        
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DoctorTextPrimary
            )
        }
        
        item {
            DoctorActionCard(
                title = "View Patients",
                description = "Manage assigned patients",
                icon = Icons.Filled.People,
                iconColor = DoctorPrimary,
                onClick = onNavigateToPatients
            )
        }
        
        item {
            DoctorActionCard(
                title = "Emergency Alerts",
                description = "View critical patient alerts",
                icon = Icons.Filled.Notifications,
                iconColor = DoctorError,
                onClick = onNavigateToEmergency
            )
        }
        
        item {
            DoctorActionCard(
                title = "Schedule",
                description = "View today's appointments",
                icon = Icons.Filled.Schedule,
                iconColor = DoctorAccent,
                onClick = onNavigateToSchedule
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DoctorTextSecondary,
                    maxLines = 1,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color = DoctorPrimary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = DoctorSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DoctorTextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DoctorTextSecondary
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = DoctorTextTertiary
            )
        }
    }
}
