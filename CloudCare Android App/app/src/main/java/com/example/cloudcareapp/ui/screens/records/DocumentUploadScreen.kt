package com.example.cloudcareapp.ui.screens.records

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.CreateMedicalRecordRequest
import com.example.cloudcareapp.data.remote.RetrofitClient
import com.example.cloudcareapp.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Document Upload Screen for Patients
 * Allows uploading medical records, lab reports, prescriptions, etc.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentUploadScreen(
    onBackClick: () -> Unit = {},
    onUploadSuccess: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedRecordType by remember { mutableStateOf("GENERAL") }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var expandedRecordType by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val patientId = AppDataCache.getPatientId()
    
    if (patientId == null) {
        // Show error if no patient ID
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Patient ID not found. Please login again.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
        }
        return
    }
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = it.lastPathSegment ?: "document"
        }
    }
    
    val recordTypes = listOf(
        "LAB_REPORT" to "Lab Report",
        "PRESCRIPTION" to "Prescription",
        "GENERAL" to "General Document",
        "CONSULTATION" to "Consultation Report",
        "IMAGING" to "Imaging/Scan"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Document", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Primary.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = Primary
                    )
                    Text(
                        text = "Upload your medical documents, lab reports, or prescriptions securely",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
            
            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Document Title") },
                placeholder = { Text("e.g., Blood Test Report") },
                leadingIcon = {
                    Icon(Icons.Filled.Title, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("Add any notes or details...") },
                leadingIcon = {
                    Icon(Icons.Filled.Description, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            // Record Type Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedRecordType,
                onExpandedChange = { expandedRecordType = it }
            ) {
                OutlinedTextField(
                    value = recordTypes.find { it.first == selectedRecordType }?.second ?: "General Document",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Document Type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRecordType)
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Category, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedRecordType,
                    onDismissRequest = { expandedRecordType = false }
                ) {
                    recordTypes.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                selectedRecordType = value
                                expandedRecordType = false
                            }
                        )
                    }
                }
            }
            
            // File Picker Button
            Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFileUri != null) Success else Secondary
                )
            ) {
                Icon(
                    imageVector = if (selectedFileUri != null) Icons.Filled.CheckCircle else Icons.Filled.AttachFile,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedFileName ?: "Select File",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            // Upload Button
            Button(
                onClick = {
                    when {
                        title.isBlank() -> {
                            Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                        }
                        selectedFileUri == null -> {
                            Toast.makeText(context, "Please select a file", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            isUploading = true
                            scope.launch {
                                try {
                                    // Read file and convert to base64
                                    val fileBase64 = withContext(Dispatchers.IO) {
                                        val inputStream: InputStream? = context.contentResolver.openInputStream(selectedFileUri!!)
                                        val bytes = inputStream?.readBytes()
                                        inputStream?.close()
                                        "data:application/octet-stream;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                                    }
                                    
                                    val request = CreateMedicalRecordRequest(
                                        patientId = patientId,
                                        title = title,
                                        description = description.ifBlank { "No description" },
                                        recordType = selectedRecordType,
                                        fileData = fileBase64
                                    )
                                    
                                    val response = withContext(Dispatchers.IO) {
                                        RetrofitClient.apiService.uploadDocument(request)
                                    }
                                    
                                    isUploading = false
                                    
                                    Toast.makeText(
                                        context,
                                        "Document uploaded successfully!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onUploadSuccess()
                                } catch (e: Exception) {
                                    isUploading = false
                                    Toast.makeText(
                                        context,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Uploading...")
                } else {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Upload Document",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            // Help Text
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Supported Formats",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "• PDF documents\n• Images (JPG, PNG)\n• Medical scans (DICOM)\n• Max file size: 10MB",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
