package com.example.cloudcareapp.ui.screens.hospital

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.widget.Toast
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.PatientQRData
import com.example.cloudcareapp.data.model.PatientSummary
import com.example.cloudcareapp.data.remote.RetrofitClient
import com.example.cloudcareapp.data.repository.HospitalRepository
import com.example.cloudcareapp.ui.components.QRScannerScreen
import com.example.cloudcareapp.ui.theme.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalPatientsScreen(
    onBackClick: () -> Unit,
    onPatientClick: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { HospitalRepository() }
    
    // Always fetch live data - no cache initialization
    var patients by remember { mutableStateOf<List<PatientSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Active, 1: Scheduled, 2: Discharged
    var searchQuery by remember { mutableStateOf("") }
    
    var showAdmitDialog by remember { mutableStateOf(false) }
    var showDischargeDialog by remember { mutableStateOf(false) }
    var selectedPatient by remember { mutableStateOf<PatientSummary?>(null) }

    fun loadPatients() {
        val hospitalId = AppDataCache.getHospitalId()
        if (hospitalId != null) {
            isLoading = true
            scope.launch {
                try {
                    // Fetch ALL patients (filter = null) to allow local filtering
                    val allPatients = repository.getPatients(hospitalId, statusFilter = null)
                    patients = allPatients
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

    LaunchedEffect(Unit) {
        loadPatients()
    }
    
    // Filter locally
    val filteredPatients = remember(patients, selectedTab, searchQuery) {
        val statusFilter = when(selectedTab) {
            0 -> "Active"
            1 -> "Scheduled"
            2 -> "Discharged"
            else -> ""
        }
        
        patients.filter { patient ->
            // Match status (case insensitive)
            // Active tab includes: Active, Emergency, Admitted, Admission Pending
            val matchesStatus = if (selectedTab == 0) {
                patient.status.equals("Active", ignoreCase = true) || 
                patient.status.equals("Emergency", ignoreCase = true) ||
                patient.status.equals("Admitted", ignoreCase = true) ||
                patient.status.contains("Admission", ignoreCase = true)
            } else if (selectedTab == 1) {
                // Scheduled tab includes: Appointment, Scheduled
                patient.status.contains("Appointment", ignoreCase = true) ||
                patient.status.equals("Scheduled", ignoreCase = true)
            } else {
                // Discharged tab
                patient.status.contains("Discharged", ignoreCase = true)
            }
            
            matchesStatus && (searchQuery.isBlank() || 
                patient.name.contains(searchQuery, ignoreCase = true) || 
                patient.id.contains(searchQuery, ignoreCase = true))
        }
    }
    
    if (showAdmitDialog) {
        AdmitPatientDialog(
            onDismiss = { showAdmitDialog = false },
            onAdmitSuccess = {
                showAdmitDialog = false
                loadPatients() // Reload list
            }
        )
    }

    if (showDischargeDialog && selectedPatient != null) {
        DischargePatientDialog(
            patient = selectedPatient!!,
            onDismiss = { 
                showDischargeDialog = false 
                selectedPatient = null
            },
            onDischargeSuccess = {
                showDischargeDialog = false
                selectedPatient = null
                loadPatients()
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAdmitDialog = true },
                    containerColor = Success,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Admit Patient")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Name, Aadhar or ID") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.White, RoundedCornerShape(8.dp)),
                singleLine = true
            )
            
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Success,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color.White
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Scheduled") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Discharged") }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                    if (filteredPatients.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No patients found", style = MaterialTheme.typography.titleMedium)
                            if (selectedTab == 0) {
                                Text("Tap + to admit a patient", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredPatients) { patient ->
                                PatientCard(
                                    patient = patient,
                                    onManageClick = {
                                        selectedPatient = patient
                                        showDischargeDialog = true
                                    },
                                    showManage = selectedTab == 0, // Only show manage for active patients,
                                    onClick = { onPatientClick(patient.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PatientCard(
    patient: PatientSummary,
    onManageClick: (PatientSummary) -> Unit,
    showManage: Boolean = true,
    onClick: () -> Unit = {}
) {
    val dateFormat = remember { 
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }
    }
    val displayDate = remember(patient.lastVisit) {
        try {
            if (patient.lastVisit != null) {
                dateFormat.format(Date.from(java.time.Instant.parse(patient.lastVisit)))
            } else {
                "N/A"
            }
        } catch (e: Exception) {
            "N/A"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        onClick = onClick
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
                    .background(
                        if (patient.status == "Emergency") Error.copy(alpha = 0.1f) else Primary.copy(alpha = 0.1f),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = if (patient.status == "Emergency") Error else Primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = patient.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${patient.age} yrs • ${patient.gender}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "Last Visit: $displayDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
            
            Badge(
                containerColor = if (patient.status == "Emergency") Error else Primary
            ) {
                Text(
                    text = patient.status,
                    modifier = Modifier.padding(4.dp),
                    color = Color.White
                )
            }
            
            if (showManage) {
                IconButton(onClick = { onManageClick(patient) }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Manage")
                }
            }
        }
    }
}

@Composable
fun AdmitPatientDialog(
    onDismiss: () -> Unit,
    onAdmitSuccess: () -> Unit
) {
    var aadharNumber by remember { mutableStateOf("") }
    var patientId by remember { mutableStateOf<String?>(null) }  // Store patient ID from QR
    var reason by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    var isAadharError by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val repository = remember { HospitalRepository() }
    val context = LocalContext.current

    if (showScanner) {
        Dialog(
            onDismissRequest = { showScanner = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                QRScannerScreen(
                    onQRCodeScanned = { qrCode ->
                        android.util.Log.d("AdmitPatient", "QR Code scanned: $qrCode")
                        showScanner = false
                        isSubmitting = true
                        error = null
                        
                        scope.launch {
                            try {
                                // Check if it's a plain 12-digit Aadhar number
                                if (qrCode.length == 12 && qrCode.all { it.isDigit() }) {
                                    android.util.Log.d("AdmitPatient", "Valid Aadhar number detected")
                                    withContext(Dispatchers.Main) {
                                        aadharNumber = qrCode
                                        isAadharError = false
                                        isSubmitting = false
                                    }
                                } else {
                                    android.util.Log.d("AdmitPatient", "Not a plain Aadhar, attempting JSON parse")
                                    // Try to parse as patient QR data
                                    try {
                                        val qrData = Gson().fromJson(qrCode, PatientQRData::class.java)
                                        android.util.Log.d("AdmitPatient", "Parsed QR type: ${qrData.type}")
                                        
                                        if (qrData.type == "patient_health_record") {
                                            // Store patient ID from QR code
                                            android.util.Log.d("AdmitPatient", "Patient ID from QR: ${qrData.patientId}")
                                            withContext(Dispatchers.Main) {
                                                patientId = qrData.patientId
                                                aadharNumber = "QR Code Scanned"  // Show indication
                                                isAadharError = false
                                                isSubmitting = false
                                                Toast.makeText(context, "Patient QR code detected", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                error = "Invalid QR code format"
                                                isSubmitting = false
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("AdmitPatient", "Failed to process QR code", e)
                                        withContext(Dispatchers.Main) {
                                            error = "Failed to read QR code: ${e.message}"
                                            isSubmitting = false
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("AdmitPatient", "Error processing QR", e)
                                withContext(Dispatchers.Main) {
                                    error = "Error: ${e.message}"
                                    isSubmitting = false
                                }
                            }
                        }
                    },
                    onDismiss = { showScanner = false }
                )
            }
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
                    "Admit Patient",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = aadharNumber,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() } && it.length <= 12) {
                            aadharNumber = it
                            isAadharError = it.length != 12
                        }
                    },
                    label = { Text("Aadhar Number") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isAadharError,
                    supportingText = {
                        if (isAadharError) {
                            Text("Must be exactly 12 digits", color = Error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        IconButton(onClick = { showScanner = true }) {
                            Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan QR")
                        }
                    }
                )
                
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Admission") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (error != null) {
                    Text(error!!, color = Error, style = MaterialTheme.typography.bodySmall)
                }
                
                if (successMessage != null) {
                    Text(successMessage!!, color = Success, style = MaterialTheme.typography.bodySmall)
                }
                
                Button(
                    onClick = {
                        val hospitalId = AppDataCache.getHospitalId()
                        // Validate: either has patient_id from QR or valid 12-digit Aadhar
                        val isValid = patientId != null || (aadharNumber.length == 12 && aadharNumber.all { it.isDigit() })
                        
                        if (hospitalId != null && isValid) {
                            isSubmitting = true
                            error = null
                            scope.launch {
                                try {
                                    val response = repository.admitPatient(
                                        hospitalId = hospitalId,
                                        aadharNumber = if (patientId == null) aadharNumber else null,
                                        patientId = patientId,
                                        reason = reason.ifEmpty { "Hospital Admission" }
                                    )
                                    if (response.success) {
                                        successMessage = "Request sent! Patient must approve."
                                        kotlinx.coroutines.delay(1500)
                                        onAdmitSuccess()
                                    } else {
                                        error = response.message
                                    }
                                } catch (e: Exception) {
                                    error = "Failed: ${e.message}"
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        } else {
                            isAadharError = patientId == null  // Only show error if no QR code scanned
                            if (patientId == null) {
                                error = "Please scan QR code or enter valid 12-digit Aadhar"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Send Admission Request")
                    }
                }
            }
        }
    }
}

@Composable
fun DischargePatientDialog(
    patient: PatientSummary,
    onDismiss: () -> Unit,
    onDischargeSuccess: () -> Unit
) {
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    var dischargeNote by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val repository = remember { HospitalRepository() }

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
                    "Manage Patient",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    "Patient: ${patient.name}",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Divider()
                
                OutlinedTextField(
                    value = dischargeNote,
                    onValueChange = { dischargeNote = it },
                    label = { Text("Discharge Note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Text(
                    "Discharge Summary Document",
                    style = MaterialTheme.typography.titleMedium
                )
                
                OutlinedButton(
                    onClick = { 
                        selectedFileName = "discharge_summary_${patient.id.take(5)}.pdf"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedFileName ?: "Upload Document (Optional)")
                }
                
                if (error != null) {
                    Text(error!!, color = Error, style = MaterialTheme.typography.bodySmall)
                }
                
                if (successMessage != null) {
                    Text(successMessage!!, color = Success, style = MaterialTheme.typography.bodySmall)
                }
                
                Button(
                    onClick = {
                        val hospitalId = AppDataCache.getHospitalId()
                        if (hospitalId != null) {
                            isSubmitting = true
                            scope.launch {
                                try {
                                    val response = repository.dischargePatient(
                                        hospitalId, 
                                        patient.id, 
                                        dischargeNote,
                                        selectedFileName
                                    )
                                    if (response.success) {
                                        successMessage = "Patient discharged successfully"
                                        kotlinx.coroutines.delay(1000)
                                        onDischargeSuccess()
                                    } else {
                                        error = response.message
                                    }
                                } catch (e: Exception) {
                                    error = "Failed: ${e.message}"
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Mark as Discharged")
                    }
                }
            }
        }
    }
}
