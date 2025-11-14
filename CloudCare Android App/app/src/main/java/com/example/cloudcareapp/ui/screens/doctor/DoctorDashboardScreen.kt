package com.example.cloudcareapp.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardScreen(
    onLogout: () -> Unit,
    onNavigateToPatients: () -> Unit = {},
    onNavigateToEmergency: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToRecords: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doctor Dashboard") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Secondary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Welcome Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Secondary
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Welcome, Dr. Suresh",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Cardiology Department",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "Today's Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DoctorStatCard(
                        title = "Patients",
                        value = "12",
                        icon = Icons.Filled.People,
                        color = Primary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToPatients
                    )
                    DoctorStatCard(
                        title = "Appointments",
                        value = "8",
                        icon = Icons.Filled.CalendarToday,
                        color = Secondary,
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
                        value = "2",
                        icon = Icons.Filled.Warning,
                        color = Error,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToEmergency
                    )
                    DoctorStatCard(
                        title = "Records",
                        value = "45",
                        icon = Icons.Filled.Description,
                        color = Success,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToRecords
                    )
                }
            }
            
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                DoctorActionCard(
                    title = "View Patients",
                    description = "Manage assigned patients",
                    icon = Icons.Filled.People,
                    onClick = onNavigateToPatients
                )
            }
            
            item {
                DoctorActionCard(
                    title = "Emergency Alerts",
                    description = "View critical patient alerts",
                    icon = Icons.Filled.Notifications,
                    onClick = onNavigateToEmergency
                )
            }
            
            item {
                DoctorActionCard(
                    title = "Schedule",
                    description = "View today's appointments",
                    icon = Icons.Filled.Schedule,
                    onClick = onNavigateToSchedule
                )
            }
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Surface
        )
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
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Secondary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Secondary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}
