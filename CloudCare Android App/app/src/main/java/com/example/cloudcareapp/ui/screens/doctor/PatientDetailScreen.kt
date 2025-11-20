package com.example.cloudcareapp.ui.screens.doctor

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.DoctorPatientResponse
import com.example.cloudcareapp.data.model.PatientProfileResponse
import com.example.cloudcareapp.data.model.TodaySummaryResponse
import com.example.cloudcareapp.data.remote.RetrofitClient
import com.example.cloudcareapp.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    patient: DoctorPatientResponse,
    onBackClick: () -> Unit,
    onRemovePatient: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var patientProfile by remember { mutableStateOf<PatientProfileResponse?>(null) }
    var healthSummary by remember { mutableStateOf<TodaySummaryResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var showRecordsScreen by remember { mutableStateOf(false) }
    
    // Show records screen if requested
    if (showRecordsScreen && patient.accessGranted) {
        PatientRecordsScreen(
            patientId = patient.patientId,
            patientName = patient.patientName,
            onBackClick = { showRecordsScreen = false }
        )
        return
    }
    
    // Load patient details
    LaunchedEffect(patient.patientId) {
        if (patient.accessGranted) {
            scope.launch {
                try {
                    loading = true
                    withContext(Dispatchers.IO) {
                        // Fetch patient profile
                        val profileResponse = RetrofitClient.apiService.getPatientProfile(patient.patientId)
                        patientProfile = profileResponse
                        
                        // Fetch health summary
                        try {
                            val summary = RetrofitClient.apiService.getTodaySummary(patient.patientId)
                            healthSummary = summary
                        } catch (e: Exception) {
                            // Health data might not be available
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to load patient details", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    loading = false
                }
            }
        } else {
            loading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(patient.patientName, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showRemoveDialog = true }) {
                        Icon(Icons.Filled.PersonRemove, "Remove Patient", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DoctorPrimary)
            )
        }
    ) { padding ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DoctorPrimary)
            }
        } else if (!patient.accessGranted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextTertiary
                    )
                    Text(
                        text = "Access Locked",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Waiting for patient consent approval",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "Status: ${patient.status}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .background(Background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Basic Info Card
                patientProfile?.patient?.let { profile ->
                    InfoCard(
                        title = "Personal Information",
                        icon = Icons.Filled.Person
                    ) {
                        InfoRow("Name", profile.name)
                        InfoRow("Age", "${profile.age} years")
                        InfoRow("Gender", profile.gender)
                        InfoRow("Blood Group", profile.bloodType)
                        InfoRow("Phone", profile.contact)
                        InfoRow("Email", profile.email)
                    }
                    
                    // Address Card
                    InfoCard(
                        title = "Address",
                        icon = Icons.Filled.Home
                    ) {
                        InfoRow("Location", profile.address)
                    }
                    
                    // Family Contact (acting as emergency contact)
                    if (profile.familyContact.isNotEmpty()) {
                        InfoCard(
                            title = "Emergency Contact",
                            icon = Icons.Filled.ContactPhone
                        ) {
                            InfoRow("Contact", profile.familyContact)
                        }
                    }
                    
                    // Insurance
                    if (profile.insuranceProvider != null) {
                        InfoCard(
                            title = "Insurance",
                            icon = Icons.Filled.HealthAndSafety
                        ) {
                            InfoRow("Provider", profile.insuranceProvider)
                            profile.insuranceId?.let {
                                InfoRow("Policy No", it)
                            }
                        }
                    }
                    
                    // Occupation
                    if (profile.occupation != null) {
                        InfoCard(
                            title = "Occupation",
                            icon = Icons.Filled.Work
                        ) {
                            InfoRow("Job", profile.occupation)
                        }
                    }
                }
                
                // Health Summary
                healthSummary?.let { summary ->
                    InfoCard(
                        title = "Today's Health Summary",
                        icon = Icons.Filled.Favorite
                    ) {
                        InfoRow("Steps", "${summary.summary.steps.total?.toInt() ?: 0}")
                        InfoRow("Heart Rate", "${summary.summary.heart_rate.avg?.toInt() ?: 0} bpm (avg)")
                        InfoRow("Sleep", "${summary.summary.sleep?.time_asleep ?: 0.0} hours")
                        InfoRow("Calories", "${summary.summary.calories.total?.toInt() ?: 0} kcal")
                        summary.summary.distance?.let {
                            InfoRow("Distance", String.format("%.2f km", (it.total ?: 0.0)))
                        }
                    }
                }
                
                // View Medical Records Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRecordsScreen = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DoctorPrimary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "View Medical Records",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Access patient documents and history",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Remove Patient Dialog
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove Patient?") },
            text = { Text("This will revoke your access to ${patient.patientName}'s medical records. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        showRemoveDialog = false
                        onRemovePatient()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: ImageVector,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DoctorPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Divider(color = Background, thickness = 1.dp)
            
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}
