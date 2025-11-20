package com.example.cloudcareapp.ui.screens.hospital

import android.widget.Toast
import androidx.compose.foundation.background
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
import com.example.cloudcareapp.data.model.PatientProfileResponse
import com.example.cloudcareapp.data.remote.RetrofitClient
import com.example.cloudcareapp.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalPatientDetailsScreen(
    patientId: String,
    onBackClick: () -> Unit,
    onViewRecords: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var patientProfile by remember { mutableStateOf<PatientProfileResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(patientId) {
        scope.launch {
            try {
                loading = true
                withContext(Dispatchers.IO) {
                    val response = RetrofitClient.apiService.getPatientProfile(patientId)
                    patientProfile = response
                }
            } catch (e: Exception) {
                error = e.message
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load details: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                loading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(patientProfile?.patient?.name ?: "Patient Details", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Success)
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
                CircularProgressIndicator(color = Success)
            }
        } else if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = error ?: "Unknown error", color = Error)
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
                patientProfile?.patient?.let { profile ->
                    // Personal Info
                    HospitalInfoCard(
                        title = "Personal Information",
                        icon = Icons.Filled.Person
                    ) {
                        HospitalInfoRow("Name", profile.name)
                        HospitalInfoRow("Age", "${profile.age} years")
                        HospitalInfoRow("Gender", profile.gender)
                        HospitalInfoRow("Blood Group", profile.bloodType)
                        HospitalInfoRow("Phone", profile.contact)
                        HospitalInfoRow("Email", profile.email)
                    }
                    
                    // Address
                    HospitalInfoCard(
                        title = "Address",
                        icon = Icons.Filled.Home
                    ) {
                        HospitalInfoRow("Location", profile.address)
                    }
                    
                    // Emergency Contact
                    if (profile.familyContact.isNotEmpty()) {
                        HospitalInfoCard(
                            title = "Emergency Contact",
                            icon = Icons.Filled.ContactPhone
                        ) {
                            HospitalInfoRow("Contact", profile.familyContact)
                        }
                    }
                    
                    // Insurance
                    if (profile.insuranceProvider != null) {
                        HospitalInfoCard(
                            title = "Insurance",
                            icon = Icons.Filled.HealthAndSafety
                        ) {
                            HospitalInfoRow("Provider", profile.insuranceProvider)
                            profile.insuranceId?.let {
                                HospitalInfoRow("Policy No", it)
                            }
                        }
                    }

                    // View Records Button
                    Button(
                        onClick = { onViewRecords(patientId) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Success),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Description, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Medical Records")
                    }
                }
            }
        }
    }
}

@Composable
fun HospitalInfoCard(
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
                    tint = Success,
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
fun HospitalInfoRow(label: String, value: String) {
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
