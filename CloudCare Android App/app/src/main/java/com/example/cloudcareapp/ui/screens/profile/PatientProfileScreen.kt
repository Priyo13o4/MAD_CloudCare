package com.example.cloudcareapp.ui.screens.profile

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
 * Patient Profile Screen
 * Matches schema.prisma Patient model exactly
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileScreen(
    onBackClick: () -> Unit = {}
) {
    // TODO: Fetch from API - matches Patient model in schema.prisma
    val patientData = remember {
        PatientProfileData(
            id = "",
            userId = "",
            aadharUid = "",
            name = "",
            age = 0,
            gender = "",
            bloodType = "",
            contact = "",
            email = "",
            address = "",
            familyContact = "",
            insuranceProvider = null,
            insuranceId = null,
            emergency = false,
            occupation = null,
            aiAnalysis = null
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", color = Color.White) },
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
                    containerColor = Primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
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
                            .background(Primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = patientData.name.ifEmpty { "Your Name" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    Text(
                        text = "${patientData.age.takeIf { it > 0 }?.toString() ?: "Age"} • ${patientData.gender.ifEmpty { "Gender" }}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (patientData.emergency) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Error.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Emergency Contact",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            
            // Medical Information
            PatientSectionCard(title = "Medical Information") {
                PatientInfoRow(
                    icon = Icons.Filled.Bloodtype,
                    label = "Blood Type",
                    value = patientData.bloodType.ifEmpty { "Not provided" }
                )
                PatientInfoRow(
                    icon = Icons.Filled.Work,
                    label = "Occupation",
                    value = patientData.occupation ?: "Not provided"
                )
                PatientInfoRow(
                    icon = Icons.Filled.Badge,
                    label = "Aadhar UID",
                    value = patientData.aadharUid.ifEmpty { "Not linked" }
                )
            }
            
            // Contact Information
            PatientSectionCard(title = "Contact Information") {
                PatientInfoRow(
                    icon = Icons.Filled.Email,
                    label = "Email",
                    value = patientData.email.ifEmpty { "Not provided" }
                )
                PatientInfoRow(
                    icon = Icons.Filled.Phone,
                    label = "Phone",
                    value = patientData.contact.ifEmpty { "Not provided" }
                )
                PatientInfoRow(
                    icon = Icons.Filled.Home,
                    label = "Address",
                    value = patientData.address.ifEmpty { "Not provided" }
                )
                PatientInfoRow(
                    icon = Icons.Filled.ContactPhone,
                    label = "Family Contact",
                    value = patientData.familyContact.ifEmpty { "Not provided" }
                )
            }
            
            // Insurance Information
            PatientSectionCard(title = "Insurance Information") {
                PatientInfoRow(
                    icon = Icons.Filled.HealthAndSafety,
                    label = "Insurance Provider",
                    value = patientData.insuranceProvider ?: "Not provided"
                )
                PatientInfoRow(
                    icon = Icons.Filled.CardMembership,
                    label = "Insurance ID",
                    value = patientData.insuranceId ?: "Not provided"
                )
            }
            
            // AI Analysis (if available)
            if (!patientData.aiAnalysis.isNullOrEmpty()) {
                PatientSectionCard(title = "AI Health Analysis") {
                    Text(
                        text = patientData.aiAnalysis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
            
            // System Information
            PatientSectionCard(title = "System Information") {
                PatientInfoRow(
                    icon = Icons.Filled.Fingerprint,
                    label = "Patient ID",
                    value = patientData.id.ifEmpty { "Not available" }
                )
                PatientInfoRow(
                    icon = Icons.Filled.AccountCircle,
                    label = "User ID",
                    value = patientData.userId.ifEmpty { "Not available" }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PatientSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
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
                color = TextPrimary
            )
            content()
        }
    }
}

@Composable
private fun PatientInfoRow(
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
            tint = Primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Data class matching schema.prisma Patient model exactly
data class PatientProfileData(
    val id: String,
    val userId: String,
    val aadharUid: String,
    val name: String,
    val age: Int,
    val gender: String,
    val bloodType: String,
    val contact: String,
    val email: String,
    val address: String,
    val familyContact: String,
    val insuranceProvider: String?,
    val insuranceId: String?,
    val emergency: Boolean,
    val occupation: String?,
    val aiAnalysis: String?
)
