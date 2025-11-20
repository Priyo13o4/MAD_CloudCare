package com.example.cloudcareapp.ui.screens.hospital

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.DoctorSummary
import com.example.cloudcareapp.data.repository.HospitalRepository
import com.example.cloudcareapp.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalStaffScreen(
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { HospitalRepository() }
    
    // Initialize with cached data if available
    var doctors by remember { mutableStateOf(AppDataCache.getHospitalDoctors()) }
    var isLoading by remember { mutableStateOf(doctors.isEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val hospitalId = AppDataCache.getHospitalId()
        if (hospitalId != null) {
            scope.launch {
                try {
                    // This will return cached data immediately if available, or fetch from network
                    val fetchedDoctors = repository.getDoctors(hospitalId)
                    doctors = fetchedDoctors
                    isLoading = false
                } catch (e: Exception) {
                    error = e.message
                    isLoading = false
                }
            }
        } else {
            error = "Hospital ID not found"
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Success
            )
        } else if (error != null) {
            Text(
                text = error ?: "Unknown error",
                color = Error,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(doctors) { doctor ->
                    DoctorCard(doctor)
                }
            }
        }
    }
}

@Composable
fun DoctorCard(doctor: DoctorSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
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
                    .background(Success.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Success
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doctor.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = doctor.specialization,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                if (doctor.phone != null) {
                    Text(
                        text = doctor.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
            
            if (doctor.isAvailable) {
                Badge(containerColor = Success) {
                    Text("Active", modifier = Modifier.padding(4.dp))
                }
            } else {
                Badge(containerColor = Color.Gray) {
                    Text("Inactive", modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}
