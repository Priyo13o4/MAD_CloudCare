package com.example.cloudcareapp.ui.screens.doctor

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.HospitalAssociation
import com.example.cloudcareapp.data.model.HospitalProfileResponse
import com.example.cloudcareapp.data.remote.RetrofitClient
import com.example.cloudcareapp.ui.theme.*
import com.example.cloudcareapp.ui.viewmodel.DoctorProfileViewModel
import kotlinx.coroutines.launch

/**
 * Doctor Profile Screen
 * Matches schema.prisma Doctor model
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileScreen(
    onBackClick: () -> Unit = {}
) {
    val viewModel: DoctorProfileViewModel = viewModel()
    val doctorProfile by viewModel.doctorProfile.observeAsState()
    val doctorHospitals by viewModel.doctorHospitals.observeAsState(emptyList())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showEditHospitalsDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        AppDataCache.getDoctorId()?.let { doctorId ->
            viewModel.loadDoctorProfile(doctorId)
            viewModel.loadDoctorHospitals(doctorId)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White) },
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
                    containerColor = DoctorPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DoctorBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DoctorSurface)
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
                            .background(DoctorPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = doctorProfile?.firstName?.take(1) ?: "D",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = DoctorPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Dr. ${doctorProfile?.firstName ?: ""} ${doctorProfile?.lastName ?: ""}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DoctorTextPrimary
                    )
                    
                    Text(
                        text = doctorProfile?.specialization ?: "Specialist",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DoctorTextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DoctorSuccess.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Active",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = DoctorSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            // Department Info
            ProfileSectionCard(title = "Professional Information") {
                ProfileInfoRow(
                    icon = Icons.Filled.Business,
                    label = "Specialization",
                    value = doctorProfile?.specialization ?: "Not assigned"
                )
                if (doctorProfile?.subSpecialization != null) {
                    ProfileInfoRow(
                        icon = Icons.Filled.Category,
                        label = "Sub-Specialization",
                        value = doctorProfile?.subSpecialization!!
                    )
                }
                ProfileInfoRow(
                    icon = Icons.Filled.Badge,
                    label = "License No",
                    value = doctorProfile?.medicalLicenseNo ?: "Not available"
                )
                ProfileInfoRow(
                    icon = Icons.Filled.WorkHistory,
                    label = "Experience",
                    value = "${doctorProfile?.experienceYears ?: 0} years"
                )
            }
            
            // Contact Information
            ProfileSectionCard(title = "Contact Information") {
                ProfileInfoRow(
                    icon = Icons.Filled.Email,
                    label = "Email",
                    value = doctorProfile?.emailProfessional ?: "Not provided"
                )
                ProfileInfoRow(
                    icon = Icons.Filled.Phone,
                    label = "Phone",
                    value = doctorProfile?.phonePrimary ?: "Not provided"
                )
                if (doctorProfile?.city != null) {
                    ProfileInfoRow(
                        icon = Icons.Filled.LocationOn,
                        label = "Location",
                        value = "${doctorProfile?.city}, ${doctorProfile?.state}"
                    )
                }
            }
            
            // Hospital Associations
            ProfileSectionCard(title = "Associated Hospitals") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (doctorHospitals.isEmpty()) "No hospitals assigned" else "${doctorHospitals.size} hospital(s)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DoctorTextSecondary
                    )
                    TextButton(onClick = { showEditHospitalsDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Hospitals",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                }
                
                if (doctorHospitals.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        doctorHospitals.forEach { hospital ->
                            HospitalChip(
                                hospital = hospital,
                                isPrimary = hospital.isPrimary
                            )
                        }
                    }
                }
            }
            
            // System Information
            ProfileSectionCard(title = "System Information") {
                ProfileInfoRow(
                    icon = Icons.Filled.Badge,
                    label = "Doctor ID",
                    value = doctorProfile?.id ?: "Not available"
                )
                ProfileInfoRow(
                    icon = Icons.Filled.AccountCircle,
                    label = "User ID",
                    value = doctorProfile?.userId ?: "Not available"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Edit Hospitals Dialog
    if (showEditHospitalsDialog) {
        EditHospitalsDialog(
            currentHospitals = doctorHospitals,
            onDismiss = { showEditHospitalsDialog = false },
            onSave = { selectedHospitalIds ->
                AppDataCache.getDoctorId()?.let { doctorId ->
                    scope.launch {
                        android.util.Log.d("DoctorProfileScreen", "Save clicked with ${selectedHospitalIds.size} hospitals")
                        val success = viewModel.updateDoctorHospitals(doctorId, selectedHospitalIds)
                        if (success) {
                            viewModel.loadDoctorHospitals(doctorId)
                            showEditHospitalsDialog = false
                            Toast.makeText(context, "Hospitals updated successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            val errorMsg = viewModel.error.value ?: "Failed to update hospitals"
                            android.util.Log.e("DoctorProfileScreen", "Hospital update failed: $errorMsg")
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
                } ?: run {
                    Toast.makeText(context, "Doctor ID not found", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
private fun HospitalChip(
    hospital: HospitalAssociation,
    isPrimary: Boolean
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isPrimary) DoctorPrimary.copy(alpha = 0.1f) else DoctorSurface,
        border = BorderStroke(1.dp, if (isPrimary) DoctorPrimary else DoctorTextSecondary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hospital.hospitalName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = DoctorTextPrimary
                )
                Text(
                    text = "Code: ${hospital.hospitalCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = DoctorTextSecondary
                )
            }
            if (isPrimary) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = DoctorPrimary
                ) {
                    Text(
                        text = "PRIMARY",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditHospitalsDialog(
    currentHospitals: List<HospitalAssociation>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var allHospitals by remember { mutableStateOf<List<HospitalProfileResponse>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedHospitalIds: Set<String> by remember { mutableStateOf(currentHospitals.map { it.hospitalId }.toSet()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        try {
            val hospitals = RetrofitClient.apiService.searchHospitals()
            allHospitals = hospitals
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }
    
    val filteredHospitals = remember(searchQuery, allHospitals) {
        if (searchQuery.isBlank()) {
            allHospitals
        } else {
            allHospitals.filter { hospital ->
                hospital.name.contains(searchQuery, ignoreCase = true) ||
                (hospital.city?.contains(searchQuery, ignoreCase = true) ?: false) ||
                (hospital.state?.contains(searchQuery, ignoreCase = true) ?: false)
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Associated Hospitals",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search hospitals...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DoctorPrimary,
                        focusedLabelColor = DoctorPrimary
                    )
                )
                
                // Selected Hospitals Chips
                if (selectedHospitalIds.size > 0) {
                    Text(
                        text = "Selected Hospitals (${selectedHospitalIds.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = DoctorTextPrimary
                    )
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allHospitals.filter { it.id in selectedHospitalIds }.chunked(2).forEach { row: List<HospitalProfileResponse> ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { hospital ->
                                    FilterChip(
                                        selected = true,
                                        onClick = {
                                            if (selectedHospitalIds.size > 1) {
                                                selectedHospitalIds = (selectedHospitalIds as Set<String>) - hospital.id
                                            } else {
                                                Toast.makeText(context, "At least one hospital must be selected", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        label = { 
                                            Text(
                                                hospital.name,
                                                maxLines = 1,
                                                style = MaterialTheme.typography.bodySmall
                                            ) 
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Selected",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = DoctorPrimary,
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White,
                                            selectedTrailingIconColor = Color.White
                                        )
                                    )
                                }
                                // Fill remaining space if odd number
                                if (row.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                
                // Available Hospitals List
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DoctorPrimary)
                    }
                } else {
                    Text(
                        text = "Available Hospitals",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = DoctorTextPrimary
                    )
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredHospitals) { hospital ->
                            val isSelected = hospital.id in selectedHospitalIds
                            Surface(
                                onClick = {
                                    selectedHospitalIds = if (isSelected) {
                                        if (selectedHospitalIds.size > 1) {
                                            (selectedHospitalIds as Set<String>) - hospital.id
                                        } else {
                                            Toast.makeText(context, "At least one hospital must be selected", Toast.LENGTH_SHORT).show()
                                            selectedHospitalIds
                                        }
                                    } else {
                                        (selectedHospitalIds as Set<String>) + hospital.id
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) DoctorPrimary.copy(alpha = 0.1f) else DoctorSurface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) DoctorPrimary else DoctorTextSecondary.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = hospital.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = DoctorTextPrimary
                                        )
                                        Text(
                                            text = "${hospital.city ?: ""}, ${hospital.state ?: ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DoctorTextSecondary
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = DoctorPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        if (filteredHospitals.isEmpty()) {
                            item {
                                Text(
                                    text = "No hospitals found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DoctorTextSecondary,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedHospitalIds.toList()) },
                colors = ButtonDefaults.buttonColors(containerColor = DoctorPrimary),
                enabled = selectedHospitalIds.size > 0
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DoctorTextSecondary)
            }
        },
        containerColor = Color.White
    )
}

@Composable
private fun ProfileSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DoctorSurface)
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
                color = DoctorTextPrimary
            )
            content()
        }
    }
}

@Composable
private fun ProfileInfoRow(
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
            tint = DoctorPrimary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = DoctorTextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = DoctorTextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Data class matching schema.prisma Doctor model
data class DoctorProfile(
    val id: String,
    val userId: String,
    val name: String,
    val specialization: String,
    val email: String,
    val phone: String,
    val department: String,
    val joinDate: String,
    val isActive: Boolean
)
