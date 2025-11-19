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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.ui.theme.*

/**
 * Doctor Profile Screen
 * Matches schema.prisma Doctor model
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileScreen(
    onBackClick: () -> Unit = {}
) {
    // TODO: Fetch from API - matches Doctor model in schema.prisma
    val doctorData = remember {
        DoctorProfile(
            id = "",
            userId = "",
            name = "",
            specialization = "",
            email = "",
            phone = "",
            department = "",
            joinDate = "",
            isActive = true
        )
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
                        Icon(
                            imageVector = Icons.Filled.MedicalServices,
                            contentDescription = null,
                            tint = DoctorPrimary,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = doctorData.name.ifEmpty { "Doctor Name" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DoctorTextPrimary
                    )
                    
                    Text(
                        text = doctorData.specialization.ifEmpty { "Specialization" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = DoctorTextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (doctorData.isActive) DoctorSuccess.copy(alpha = 0.1f) 
                               else DoctorTextTertiary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = if (doctorData.isActive) "Active" else "Inactive",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (doctorData.isActive) DoctorSuccess else DoctorTextTertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            // Department Info
            ProfileSectionCard(title = "Department Information") {
                ProfileInfoRow(
                    icon = Icons.Filled.Business,
                    label = "Department",
                    value = doctorData.department.ifEmpty { "Not assigned" }
                )
                ProfileInfoRow(
                    icon = Icons.Filled.CalendarToday,
                    label = "Join Date",
                    value = doctorData.joinDate.ifEmpty { "Not available" }
                )
            }
            
            // Contact Information
            ProfileSectionCard(title = "Contact Information") {
                ProfileInfoRow(
                    icon = Icons.Filled.Email,
                    label = "Email",
                    value = doctorData.email.ifEmpty { "Not provided" }
                )
                ProfileInfoRow(
                    icon = Icons.Filled.Phone,
                    label = "Phone",
                    value = doctorData.phone.ifEmpty { "Not provided" }
                )
            }
            
            // System Information
            ProfileSectionCard(title = "System Information") {
                ProfileInfoRow(
                    icon = Icons.Filled.Badge,
                    label = "Doctor ID",
                    value = doctorData.id.ifEmpty { "Not available" }
                )
                ProfileInfoRow(
                    icon = Icons.Filled.AccountCircle,
                    label = "User ID",
                    value = doctorData.userId.ifEmpty { "Not available" }
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
