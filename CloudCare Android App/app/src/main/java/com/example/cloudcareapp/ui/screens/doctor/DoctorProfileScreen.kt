package com.example.cloudcareapp.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.ui.theme.*
import com.example.cloudcareapp.ui.viewmodel.DoctorProfileViewModel

/**
 * Doctor Profile Screen
 * Matches schema.prisma Doctor model
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileScreen(
    onBackClick: () -> Unit = {}
) {
    val viewModel: DoctorProfileViewModel = viewModel()
    val doctorProfile by viewModel.doctorProfile.observeAsState()
    
    LaunchedEffect(Unit) {
        AppDataCache.getDoctorId()?.let { doctorId ->
            viewModel.loadDoctorProfile(doctorId)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Edit profile */ }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DoctorPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DoctorBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DoctorSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(DoctorPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = doctorProfile?.firstName?.take(1) ?: "D",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = DoctorPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Dr. ${doctorProfile?.firstName ?: ""} ${doctorProfile?.lastName ?: ""}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DoctorTextPrimary
                    )
                    
                    Text(
                        text = doctorProfile?.specialization ?: "Specialist",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DoctorTextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DoctorSuccess.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Active",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = DoctorSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            // Department Info
            ProfileSectionCard(title = "Professional Information") {
                ProfileInfoRow(
                    icon = Icons.Filled.Business,
                    label = "Specialization",
                    value = doctorProfile?.specialization ?: "Not assigned"
                )
                if (doctorProfile?.subSpecialization != null) {
                    ProfileInfoRow(
                        icon = Icons.Filled.Category,
                        label = "Sub-Specialization",
                        value = doctorProfile?.subSpecialization!!
                    )
                }
                ProfileInfoRow(
                    icon = Icons.Filled.Badge,
                    label = "License No",
                    value = doctorProfile?.medicalLicenseNo ?: "Not available"
                )
                ProfileInfoRow(
                    icon = Icons.Filled.WorkHistory,
                    label = "Experience",
                    value = "${doctorProfile?.experienceYears ?: 0} years"
                )
            }
            
            // Contact Information
            ProfileSectionCard(title = "Contact Information") {
                ProfileInfoRow(
                    icon = Icons.Filled.Email,
                    label = "Email",
                    value = doctorProfile?.emailProfessional ?: "Not provided"
                )
                ProfileInfoRow(
                    icon = Icons.Filled.Phone,
                    label = "Phone",
                    value = doctorProfile?.phonePrimary ?: "Not provided"
                )
                if (doctorProfile?.city != null) {
                    ProfileInfoRow(
                        icon = Icons.Filled.LocationOn,
                        label = "Location",
                        value = "${doctorProfile?.city}, ${doctorProfile?.state}"
                    )
                }
            }
            
            // System Information
            ProfileSectionCard(title = "System Information") {
                ProfileInfoRow(
                    icon = Icons.Filled.Badge,
                    label = "Doctor ID",
                    value = doctorProfile?.id ?: "Not available"
                )
                ProfileInfoRow(
                    icon = Icons.Filled.AccountCircle,
                    label = "User ID",
                    value = doctorProfile?.userId ?: "Not available"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DoctorSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DoctorTextPrimary
            )
            content()
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DoctorPrimary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = DoctorTextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = DoctorTextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Data class matching schema.prisma Doctor model
data class DoctorProfile(
    val id: String,
    val userId: String,
    val name: String,
    val specialization: String,
    val email: String,
    val phone: String,
    val department: String,
    val joinDate: String,
    val isActive: Boolean
)
