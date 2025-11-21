package com.example.cloudcareapp.ui.screens.records

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.window.Dialog
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.HospitalProfileResponse
import com.example.cloudcareapp.data.model.MedicalRecordResponse
import com.example.cloudcareapp.data.model.MedicalRecordSummary
import com.example.cloudcareapp.data.model.RecordLookupRequest
import com.example.cloudcareapp.data.remote.RetrofitClient
import com.example.cloudcareapp.ui.theme.*
import com.example.cloudcareapp.utils.FileDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    patientId: String? = null,
    onNavigateToUpload: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var documents by remember { mutableStateOf<List<MedicalRecordResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    
    var showLookupDialog by remember { mutableStateOf(false) }
    var lookupResults by remember { mutableStateOf<List<MedicalRecordSummary>?>(null) }
    
    val targetPatientId = patientId ?: remember { AppDataCache.getPatientId() }
    val isViewerMode = patientId != null
    
    // ✅ FIX: Load real documents from API
    fun loadDocuments() {
        if (targetPatientId == null) {
            errorMessage = "Patient ID not found."
            isLoading = false
            return
        }
        
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val docs = RetrofitClient.apiService.getPatientDocuments(targetPatientId)
                    withContext(Dispatchers.Main) {
                        documents = docs
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = e.message ?: "Failed to load documents"
                    isLoading = false
                }
            }
        }
    }
    
    // Load on first composition
    LaunchedEffect(targetPatientId) {
        loadDocuments()
    }
    
    // Filter documents by type
    val filteredDocuments = remember(documents, selectedTab) {
        when (selectedTab) {
            0 -> documents // All
            1 -> documents.filter { it.recordType == "LAB_REPORT" }
            2 -> documents.filter { it.recordType == "PRESCRIPTION" }
            else -> documents
        }
    }
    
    if (showLookupDialog) {
        RecordLookupDialog(
            onDismiss = { showLookupDialog = false },
            onResultsFound = { results ->
                lookupResults = results
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            if (!isViewerMode) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.End) {
                    FloatingActionButton(
                        onClick = { showLookupDialog = true },
                        containerColor = Secondary,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = "Lookup Records")
                    }
                    
                    FloatingActionButton(
                        onClick = onNavigateToUpload,
                        containerColor = Primary,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Filled.UploadFile, contentDescription = "Upload Document")
                    }
                }
            }
        }
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(innerPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Health Records",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isLoading) "Loading..." else "Total: ${documents.size} records",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { loadDocuments() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
            }
        }
        
        // Error Banner
        if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ErrorLight.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Unable to load records",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Error
                        )
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = Error.copy(alpha = 0.8f)
                        )
                    }
                    TextButton(onClick = { loadDocuments() }) {
                        Text("Try Again")
                    }
                }
            }
        }
        
        // Tab Row
        val labCount = documents.count { it.recordType == "LAB_REPORT" }
        val prescriptionCount = documents.count { it.recordType == "PRESCRIPTION" }
        
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Surface,
            contentColor = Primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("All (${documents.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Lab Reports ($labCount)") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Prescriptions ($prescriptionCount)") }
            )
        }
        
        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            filteredDocuments.isEmpty() && lookupResults == null -> {
                // Empty State
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = TextTertiary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (errorMessage != null) "Unable to load records" else "No records found",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (errorMessage != null) "Please check your connection and try again." else "Upload your first document using the + button",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { loadDocuments() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Retry Loading")
                            }
                        }
                    }
                }
            }
            
            else -> {
                // Documents List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Show lookup results if any
                    if (lookupResults != null) {
                        item {
                            Text(
                                "Found Records",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                        items(lookupResults!!) { record ->
                            LookupRecordCard(record)
                        }
                        item {
                            Divider()
                        }
                    }

                    items(filteredDocuments) { document ->
                        DocumentCard(document)
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun DocumentCard(document: MedicalRecordResponse) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val displayDate = remember(document.date) {
        try {
            dateFormat.format(Date.from(java.time.Instant.parse(document.date)))
        } catch (e: Exception) {
            document.date
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon based on type
            val (icon, iconColor) = when (document.recordType) {
                "LAB_REPORT" -> Icons.Filled.Science to Color(0xFF3B82F6)
                "PRESCRIPTION" -> Icons.Filled.LocalPharmacy to Color(0xFF10B981)
                "CONSULTATION" -> Icons.Filled.MedicalServices to Color(0xFFF59E0B)
                "IMAGING" -> Icons.Filled.Camera to Color(0xFF8B5CF6)
                else -> Icons.Filled.Description to Primary
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = document.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
            
            IconButton(onClick = { 
                if (!document.fileUrl.isNullOrEmpty()) {
                    scope.launch(Dispatchers.IO) {
                        val fileName = FileDownloader.generateFileName(
                            document.title,
                            document.recordType
                        )
                        val mimeType = FileDownloader.getMimeTypeFromExtension(fileName)
                        
                        withContext(Dispatchers.Main) {
                            FileDownloader.downloadAndOpenBase64File(
                                context,
                                document.fileUrl,
                                fileName,
                                mimeType
                            )
                        }
                    }
                } else {
                    Toast.makeText(context, "No file attached", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Download",
                    tint = if (!document.fileUrl.isNullOrEmpty()) Primary else Color.Gray
                )
            }
        }
    }
}

@Composable
private fun LookupRecordCard(record: MedicalRecordSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Secondary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = record.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2
                )
                if (record.doctorName != null) {
                    Text(
                        text = record.doctorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordLookupDialog(
    onDismiss: () -> Unit,
    onResultsFound: (List<MedicalRecordSummary>) -> Unit
) {
    var hospitals by remember { mutableStateOf<List<HospitalProfileResponse>>(emptyList()) }
    var selectedHospital by remember { mutableStateOf<HospitalProfileResponse?>(null) }
    var aadharNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            withContext(Dispatchers.IO) {
                val list = RetrofitClient.apiService.getHospitals()
                withContext(Dispatchers.Main) {
                    hospitals = list
                    isLoading = false
                }
            }
        } catch (e: Exception) {
            isLoading = false
            error = "Failed to load hospitals"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Find Old Records",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    // Hospital Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedHospital?.name ?: "Select Hospital",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            hospitals.forEach { hospital ->
                                DropdownMenuItem(
                                    text = { Text(hospital.name) },
                                    onClick = {
                                        selectedHospital = hospital
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = aadharNumber,
                        onValueChange = { aadharNumber = it },
                        label = { Text("Aadhar Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (error != null) {
                        Text(error!!, color = Error, style = MaterialTheme.typography.bodySmall)
                    }
                    
                    Button(
                        onClick = {
                            if (selectedHospital != null && aadharNumber.isNotEmpty()) {
                                isSearching = true
                                error = null
                                scope.launch {
                                    try {
                                        val results = withContext(Dispatchers.IO) {
                                            RetrofitClient.apiService.lookupPatientRecords(
                                                RecordLookupRequest(
                                                    hospitalId = selectedHospital!!.id,
                                                    aadharNumber = aadharNumber
                                                )
                                            )
                                        }
                                        onResultsFound(results)
                                        onDismiss()
                                    } catch (e: Exception) {
                                        error = "Failed to find records: ${e.message}"
                                    } finally {
                                        isSearching = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSearching
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Search Records")
                        }
                    }
                }
            }
        }
    }
}
