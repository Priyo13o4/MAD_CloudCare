package com.example.cloudcareapp.ui.screens.hospital

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalProfileScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hospital Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hospital Header
            item {
                HospitalHeader()
            }
            
            // Hospital Information Section
            item {
                SectionTitle("Hospital Information")
            }
            
            item {
                HospitalInfoCard(
                    icon = Icons.Filled.LocalHospital,
                    label = "Hospital Name",
                    value = "Apollo Hospital"
                )
            }
            
            item {
                HospitalInfoCard(
                    icon = Icons.Filled.LocationOn,
                    label = "Location",
                    value = "Bangalore, Karnataka"
                )
            }
            
            item {
                HospitalInfoCard(
                    icon = Icons.Filled.Phone,
                    label = "Phone",
                    value = "+91 80 4060 0000"
                )
            }
            
            item {
                HospitalInfoCard(
                    icon = Icons.Filled.Email,
                    label = "Email",
                    value = "contact@apollohospital.com"
                )
            }
            
            // Statistics Section
            item {
                SectionTitle("Statistics")
            }
            
            item {
                HospitalInfoCard(
                    icon = Icons.Filled.People,
                    label = "Total Doctors",
                    value = "24"
                )
            }
            
            item {
                HospitalInfoCard(
                    icon = Icons.Filled.Hotel,
                    label = "Total Beds",
                    value = "150"
                )
            }
            
            item {
                HospitalInfoCard(
                    icon = Icons.Filled.Visibility,
                    label = "Current Occupancy",
                    value = "87%"
                )
            }
            
            // Services Section
            item {
                SectionTitle("Services")
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceChip("Cardiology")
                    ServiceChip("Orthopedics")
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceChip("Neurology")
                    ServiceChip("Pediatrics")
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceChip("General Surgery")
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HospitalHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Success.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Success.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalHospital,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            Column {
                Text(
                    text = "Apollo Hospital",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bangalore, Karnataka",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )
}

@Composable
private fun HospitalInfoCard(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Success,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun ServiceChip(service: String) {
    AssistChip(
        onClick = { },
        label = { Text(service) },
        modifier = Modifier.padding(end = 4.dp, bottom = 8.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Success.copy(alpha = 0.1f),
            labelColor = Success
        )
    )
}
