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
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.PatientQRData
import com.example.cloudcareapp.data.model.PatientSummary
import com.example.cloudcareapp.data.repository.HospitalRepository
import com.example.cloudcareapp.ui.components.QRScannerScreen
import com.example.cloudcareapp.ui.theme.*
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalAdmissionsScreen(
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { HospitalRepository() }
    
    var patients by remember { mutableStateOf<List<PatientSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    var showAdmitDialog by remember { mutableStateOf(false) }
    var showDischargeDialog by remember { mutableStateOf(false) }
    var selectedPatient by remember { mutableStateOf<PatientSummary?>(null) }

    fun loadPatients() {
        val hospitalId = AppDataCache.getHospitalId()
        if (hospitalId != null) {
            isLoading = true
            scope.launch {
                try {
                    patients = repository.getPatients(hospitalId)
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
        topBar = {
            TopAppBar(
                title = { Text("Admissions & Patients") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Success,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdmitDialog = true },
                containerColor = Success,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Admit Patient")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
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
                if (patients.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No patients found", style = MaterialTheme.typography.titleMedium)
                        Text("Tap + to admit a patient", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(patients) { patient ->
                            PatientCard(
                                patient = patient,
                                onManageClick = {
                                    selectedPatient = patient
                                    showDischargeDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


