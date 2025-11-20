package com.example.cloudcareapp.ui.screens.doctor

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.data.model.MedicalRecordResponse
import com.example.cloudcareapp.data.remote.RetrofitClient
import com.example.cloudcareapp.ui.theme.*
import com.example.cloudcareapp.utils.TimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRecordsScreen(
    patientId: String,
    patientName: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var records by remember { mutableStateOf<List<MedicalRecordResponse>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedRecord by remember { mutableStateOf<MedicalRecordResponse?>(null) }
    
    // Load records
    LaunchedEffect(patientId) {
        scope.launch {
            try {
                loading = true
                withContext(Dispatchers.IO) {
                    records = RetrofitClient.apiService.getPatientDocuments(patientId)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load records: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                loading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Medical Records", color = Color.White)
                        Text(
                            text = patientName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
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
        } else if (records.isEmpty()) {
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
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextTertiary
                    )
                    Text(
                        text = "No Medical Records",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Patient hasn't uploaded any medical records yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DoctorPrimary.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = DoctorPrimary
                            )
                            Text(
                                text = "${records.size} medical record(s) found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
                
                items(records) { record ->
                    MedicalRecordCard(
                        record = record,
                        onClick = { selectedRecord = record }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
    
    // Record detail dialog
    if (selectedRecord != null) {
        RecordDetailDialog(
            record = selectedRecord!!,
            onDismiss = { selectedRecord = null }
        )
    }
}

@Composable
fun MedicalRecordCard(
    record: MedicalRecordResponse,
    onClick: () -> Unit
) {
    val recordTypeColor = when (record.recordType.uppercase()) {
        "LAB_REPORT", "LAB REPORT" -> Secondary
        "PRESCRIPTION" -> Success
        "DIAGNOSIS" -> Warning
        "IMAGING", "X-RAY", "MRI", "CT_SCAN" -> Primary
        "DISCHARGE_SUMMARY" -> DoctorPrimary
        else -> TextSecondary
    }
    
    val recordIcon = when (record.recordType.uppercase()) {
        "LAB_REPORT", "LAB REPORT" -> Icons.Filled.Science
        "PRESCRIPTION" -> Icons.Filled.Medication
        "DIAGNOSIS" -> Icons.Filled.Assignment
        "IMAGING", "X-RAY", "MRI", "CT_SCAN" -> Icons.Filled.Image
        "DISCHARGE_SUMMARY" -> Icons.Filled.Note
        else -> Icons.Filled.Description
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = recordTypeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = recordIcon,
                    contentDescription = null,
                    tint = recordTypeColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Text(
                    text = record.recordType.replace("_", " ").replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase() else it.toString() 
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = recordTypeColor,
                    fontWeight = FontWeight.Medium
                )
                
                if (record.description.isNotEmpty()) {
                    Text(
                        text = record.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TextTertiary
                    )
                    Text(
                        text = TimeFormatter.parseUtcToIst(record.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
            
            // View indicator
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "View",
                tint = TextSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailDialog(
    record: MedicalRecordResponse,
    onDismiss: () -> Unit
) {
    val recordTypeColor = when (record.recordType.uppercase()) {
        "LAB_REPORT", "LAB REPORT" -> Secondary
        "PRESCRIPTION" -> Success
        "DIAGNOSIS" -> Warning
        "IMAGING", "X-RAY", "MRI", "CT_SCAN" -> Primary
        "DISCHARGE_SUMMARY" -> DoctorPrimary
        else -> TextSecondary
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = record.recordType.replace("_", " ").replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase() else it.toString() 
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = recordTypeColor,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Divider()
                
                // Date
                InfoRow(
                    icon = Icons.Filled.CalendarToday,
                    label = "Date",
                    value = TimeFormatter.parseUtcToIst(record.date)
                )
                
                // Description
                if (record.description.isNotEmpty()) {
                    InfoRow(
                        icon = Icons.Filled.Description,
                        label = "Description",
                        value = record.description
                    )
                }
                
                // Facility
                if (record.facilityId != null) {
                    InfoRow(
                        icon = Icons.Filled.LocalHospital,
                        label = "Facility",
                        value = record.facilityId
                    )
                }
                
                // Uploaded
                InfoRow(
                    icon = Icons.Filled.Upload,
                    label = "Uploaded",
                    value = TimeFormatter.parseUtcToIst(record.createdAt)
                )
                
                // File info
                if (record.fileUrl != null) {
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AttachFile,
                            contentDescription = null,
                            tint = DoctorPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Document attached",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DoctorPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
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
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
