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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cloudcareapp.ui.viewmodel.HospitalProfileViewModel
import androidx.compose.runtime.livedata.observeAsState
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.HospitalProfileResponse
import com.example.cloudcareapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalProfileScreen(
    onBackClick: () -> Unit
) {
    val viewModel: HospitalProfileViewModel = viewModel()
    val hospitalProfile by viewModel.hospitalProfile.observeAsState()
    
    LaunchedEffect(Unit) {
        AppDataCache.getHospitalId()?.let { hospitalId ->
            viewModel.loadHospitalProfile(hospitalId)
        }
    }

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
                HospitalHeader(hospitalProfile)
            }
            
            // Hospital Information Section
            item {
                SectionTitle("Hospital Information")
            }
            
            item {
                HospitalInfoCard(
                    icon = Icons.Filled.LocalHospital,
                    label = "Hospital Name",
                    value = hospitalProfile?.name ?: "Loading..."
                )
            }
            
            item {
                HospitalInfoCard(
                    icon = Icons.Filled.QrCode,
                    label = "Hospital Code",
                    value = hospitalProfile?.hospitalCode ?: "N/A"
                )
            }
            
            item {
                HospitalInfoCard(
                    icon = Icons.Filled.LocationOn,
                    label = "Location",
                    value = if (hospitalProfile?.city != null) "${hospitalProfile?.city}, ${hospitalProfile?.state}" else "Loading..."
                )
            }
            
            item {
                HospitalInfoCard(
                    icon = Icons.Filled.Phone,
                    label = "Phone",
                    value = hospitalProfile?.phonePrimary ?: "Loading..."
                )
            }
            
            item {
                HospitalInfoCard(
                    icon = Icons.Filled.Email,
                    label = "Email",
                    value = hospitalProfile?.email ?: "Loading..."
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
                    value = hospitalProfile?.totalDoctors?.toString() ?: "0"
                )
            }
            
            item {
                HospitalInfoCard(
                    icon = Icons.Filled.Hotel,
                    label = "Total Beds",
                    value = hospitalProfile?.totalBeds?.toString() ?: "0"
                )
            }
            
            item {
                HospitalInfoCard(
                    icon = Icons.Filled.Visibility,
                    label = "Available Beds",
                    value = hospitalProfile?.availableBeds?.toString() ?: "0"
                )
            }
            
            // Services Section
            item {
                SectionTitle("Services")
            }
            
            item {
                val services = hospitalProfile?.specializations?.let { specs ->
                    specs.replace("[", "").replace("]", "").replace("\"", "").split(",")
                } ?: emptyList()
                
                if (services.isNotEmpty()) {
                     Column {
                        services.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { service ->
                                    ServiceChip(service.trim())
                                }
                            }
                        }
                     }
                } else {
                     Text("No services listed", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HospitalHeader(hospitalProfile: HospitalProfileResponse?) {
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
                    text = hospitalProfile?.name ?: "Loading...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (hospitalProfile?.city != null) "${hospitalProfile.city}, ${hospitalProfile.state}" else "Loading...",
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
