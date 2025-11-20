package com.example.cloudcareapp.ui.screens.doctor

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.CreateConsentRequest
import com.example.cloudcareapp.data.model.PatientQRData
import com.example.cloudcareapp.data.remote.RetrofitClient
import com.example.cloudcareapp.ui.components.QRScannerScreen
import com.example.cloudcareapp.ui.theme.DoctorPrimary
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * QR Scanner Screen for Doctor
 * Scans patient QR code and creates consent request
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorQRScannerScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showScanner by remember { mutableStateOf(true) }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // ✅ FIX: Get doctor ID from cache
    val doctorId: String? = remember { AppDataCache.getDoctorId() }
    // Doctor name will be fetched from profile or use email as fallback
    val doctorName: String = remember { 
        AppDataCache.getUserName() ?: "Healthcare Professional" 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Patient QR Code", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DoctorPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
        ) {
            when {
                showScanner && !isProcessing -> {
                    // Show QR scanner
                    QRScannerScreen(
                        onQRCodeScanned = { qrCode ->
                            showScanner = false
                            isProcessing = true
                            
                            scope.launch {
                                try {
                                    // Parse QR code data
                                    val qrData = Gson().fromJson(qrCode, PatientQRData::class.java)
                                    
                                    if (qrData.type != "patient_health_record") {
                                        errorMessage = "Invalid QR code type"
                                        isProcessing = false
                                        return@launch
                                    }
                                    
                                    if (doctorId == null) {
                                        errorMessage = "Doctor ID not found. Please login again."
                                        isProcessing = false
                                        return@launch
                                    }
                                    
                                    // Create consent request
                                    withContext(Dispatchers.IO) {
                                        val request = CreateConsentRequest(
                                            patientId = qrData.patientId,
                                            doctorId = doctorId,
                                            facilityName = doctorName,
                                            requestType = "DATA_ACCESS",
                                            description = "Request to access your medical records for consultation",
                                            expiresInDays = 90
                                        )
                                        
                                        val response = RetrofitClient.apiService.createConsentRequest(request)
                                        
                                        withContext(Dispatchers.Main) {
                                            // ✅ FIX: Check if this is a duplicate consent request
                                            if (response.status == "PENDING") {
                                                Toast.makeText(
                                                    context,
                                                    "Consent request already waiting for approval",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                scanResult = "Consent request already pending"
                                            } else {
                                                scanResult = "Success! Consent request sent to patient."
                                                Toast.makeText(
                                                    context,
                                                    "Patient added to your dashboard. Awaiting consent approval.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                            isProcessing = false
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        errorMessage = e.message ?: "Failed to process QR code"
                                        isProcessing = false
                                    }
                                }
                            }
                        },
                        onDismiss = onBackClick
                    )
                }
                
                isProcessing -> {
                    // Show loading
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = DoctorPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Creating consent request...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                scanResult != null -> {
                    // Show success message
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(64.dp)
                                )
                                
                                Text(
                                    text = "Success!",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                                
                                Text(
                                    text = scanResult!!,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF374151)
                                )
                                
                                Text(
                                    text = "The patient will appear in your dashboard with locked data until they approve your request.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF6B7280)
                                )
                                
                                Button(
                                    onClick = onBackClick,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DoctorPrimary
                                    )
                                ) {
                                    Text("Back to Dashboard")
                                }
                            }
                        }
                    }
                }
                
                errorMessage != null -> {
                    // Show error message
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Error",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                                
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF374151)
                                )
                                
                                Button(
                                    onClick = {
                                        errorMessage = null
                                        showScanner = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DoctorPrimary
                                    )
                                ) {
                                    Text("Try Again")
                                }
                                
                                OutlinedButton(
                                    onClick = onBackClick,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
