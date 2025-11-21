package com.example.cloudcareapp.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cloudcareapp.data.model.*
import com.example.cloudcareapp.data.remote.CloudCareApiService
import com.example.cloudcareapp.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorSignupScreen(
    onBackClick: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    // Form state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var medicalLicenseNo by remember { mutableStateOf("") }
    var registrationYear by remember { mutableStateOf("") }
    var registrationState by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("Dr.") }
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var countryCodePrimary by remember { mutableStateOf("+91") }
    var phonePrimary by remember { mutableStateOf("") }
    var countryCodeSecondary by remember { mutableStateOf("+91") }
    var phoneSecondary by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var qualifications by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }
    var selectedHospitals by remember { mutableStateOf<List<HospitalProfileResponse>>(emptyList()) }
    var consultationFee by remember { mutableStateOf("") }
    var availableForEmergency by remember { mutableStateOf(false) }
    var telemedicineEnabled by remember { mutableStateOf(false) }
    var languages by remember { mutableStateOf("") }
    var clinicAddress by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    
    // Hospital search state
    var hospitalSearchQuery by remember { mutableStateOf("") }
    var allHospitals by remember { mutableStateOf<List<HospitalProfileResponse>>(emptyList()) }
    var hospitalSearchResults by remember { mutableStateOf<List<HospitalProfileResponse>>(emptyList()) }
    var isLoadingHospitals by remember { mutableStateOf(false) }
    var showHospitalDropdown by remember { mutableStateOf(false) }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(1) }
    
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val scope = rememberCoroutineScope()
    
    val authState by authViewModel.authState.observeAsState()
    val isLoading by authViewModel.isLoading.observeAsState(false)
    val errorMessage by authViewModel.errorMessage.observeAsState()
    
    // Initialize API service
    val apiService = remember {
        com.example.cloudcareapp.data.remote.RetrofitClient.apiService
    }
    
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthViewModel.AuthState.Success -> {
                Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                onSignupSuccess()
            }
            is AuthViewModel.AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }
    
    // Load all hospitals on screen mount
    LaunchedEffect(Unit) {
        if (allHospitals.isEmpty()) {
            try {
                isLoadingHospitals = true
                allHospitals = apiService.getHospitals()
            } catch (e: Exception) {
                allHospitals = emptyList()
                Toast.makeText(context, "Failed to load hospitals", Toast.LENGTH_SHORT).show()
            } finally {
                isLoadingHospitals = false
            }
        }
    }
    
    // Filter hospitals based on search query (local filtering, no API calls)
    fun filterHospitals(query: String) {
        hospitalSearchResults = if (query.isBlank()) {
            allHospitals
        } else {
            allHospitals.filter { hospital ->
                hospital.name.contains(query, ignoreCase = true) ||
                (hospital.hospitalCode?.contains(query, ignoreCase = true) ?: false)
            }
        }
    }
    
    val totalSteps = 3
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF5F7FA),
                            Color.White
                        )
                    )
                )
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = Color(0xFF1A1A1A))
                }
                Text(
                    text = "Doctor Registration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            LinearProgressIndicator(
                progress = currentStep.toFloat() / totalSteps,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                color = Color(0xFF8B5CF6),
                trackColor = Color(0xFFE5E7EB)
            )
            
            Text(
                text = "Step $currentStep of $totalSteps",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                when (currentStep) {
                    1 -> {
                        Text(
                            "Account & Professional Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address *") },
                            leadingIcon = { Icon(Icons.Outlined.Email, null) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password *") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, null)
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { 
                                confirmPassword = it
                                passwordError = false
                            },
                            label = { Text("Confirm Password *") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(if (confirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, null)
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp),
                            isError = passwordError,
                            supportingText = if (passwordError) {
                                { Text("Passwords don't match", color = MaterialTheme.colorScheme.error) }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = if (passwordError) MaterialTheme.colorScheme.error else Color(0xFFD1D5DB),
                                errorBorderColor = MaterialTheme.colorScheme.error
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = medicalLicenseNo,
                            onValueChange = { medicalLicenseNo = it },
                            label = { Text("Medical License Number *") },
                            leadingIcon = { Icon(Icons.Outlined.Badge, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = registrationYear,
                                onValueChange = { registrationYear = it },
                                label = { Text("Reg. Year *") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF8B5CF6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = registrationState,
                                onValueChange = { registrationState = it },
                                label = { Text("State *") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF8B5CF6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Hospital Search Field with Chips
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Selected Hospital Chips
                            if (selectedHospitals.isNotEmpty()) {
                                Text(
                                    "Selected Hospitals",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF6B7280),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    selectedHospitals.forEach { hospital ->
                                        FilterChip(
                                            selected = true,
                                            onClick = {
                                                selectedHospitals = selectedHospitals.filter { it.id != hospital.id }
                                            },
                                            label = { 
                                                Text(
                                                    hospital.name,
                                                    maxLines = 1,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            },
                                            trailingIcon = {
                                                Icon(
                                                    Icons.Filled.Close,
                                                    contentDescription = "Remove",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF8B5CF6),
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                            
                            // Search Box
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = hospitalSearchQuery,
                                    onValueChange = { query ->
                                        hospitalSearchQuery = query
                                        filterHospitals(query)
                                        if (query.isNotEmpty()) {
                                            showHospitalDropdown = true
                                        }
                                    },
                                    label = { Text("Add Hospital *") },
                                    placeholder = { Text("Search by name or code...") },
                                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                                    trailingIcon = {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (hospitalSearchQuery.isNotEmpty()) {
                                                IconButton(
                                                    onClick = {
                                                        hospitalSearchQuery = ""
                                                        filterHospitals("")
                                                        showHospitalDropdown = false
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Filled.Clear, null, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                            if (isLoadingHospitals) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                            } else {
                                                IconButton(
                                                    onClick = { 
                                                        showHospitalDropdown = !showHospitalDropdown
                                                        if (showHospitalDropdown && hospitalSearchQuery.isEmpty()) {
                                                            filterHospitals("")
                                                        }
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        if (showHospitalDropdown) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                                        null,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF8B5CF6),
                                        unfocusedBorderColor = Color(0xFFD1D5DB)
                                    )
                                )
                                
                                // Dropdown Menu
                                if (showHospitalDropdown && !isLoadingHospitals) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 64.dp)
                                            .heightIn(max = 250.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                    ) {
                                        Column {
                                            // Header with close button
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFF9FAFB))
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Available Hospitals (${hospitalSearchResults.size})",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Color(0xFF6B7280)
                                                )
                                                IconButton(
                                                    onClick = { showHospitalDropdown = false },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Close,
                                                        "Close",
                                                        tint = Color(0xFF6B7280),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            
                                            Divider(color = Color(0xFFE5E7EB))
                                            
                                            if (hospitalSearchResults.isEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(32.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        "No hospitals found",
                                                        color = Color(0xFF6B7280),
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            } else {
                                                LazyColumn {
                                                    items(hospitalSearchResults) { hospital ->
                                                        val isSelected = selectedHospitals.any { it.id == hospital.id }
                                                        HospitalDropdownItem(
                                                            hospital = hospital,
                                                            isSelected = isSelected,
                                                            onClick = {
                                                                selectedHospitals = if (isSelected) {
                                                                    selectedHospitals.filter { it.id != hospital.id }
                                                                } else {
                                                                    selectedHospitals + hospital
                                                                }
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    2 -> {
                        Text(
                            "Personal & Contact Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Title *") },
                                placeholder = { Text("Dr.") },
                                modifier = Modifier.weight(0.4f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF8B5CF6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { Text("First Name *") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF8B5CF6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = middleName,
                                onValueChange = { middleName = it },
                                label = { Text("Middle Name") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF8B5CF6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = { Text("Last Name *") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF8B5CF6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Primary Phone with Country Code
                        Text(
                            text = "Primary Phone *",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            var expandedPrimary by remember { mutableStateOf(false) }
                            val countryCodes = listOf("+91", "+1", "+44", "+61", "+86")
                            
                            Box(modifier = Modifier.width(100.dp)) {
                                OutlinedTextField(
                                    value = countryCodePrimary,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            Icons.Filled.ArrowDropDown,
                                            null,
                                            modifier = Modifier.clickable { expandedPrimary = !expandedPrimary }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF8B5CF6),
                                        unfocusedBorderColor = Color(0xFFD1D5DB)
                                    )
                                )
                                DropdownMenu(
                                    expanded = expandedPrimary,
                                    onDismissRequest = { expandedPrimary = false }
                                ) {
                                    countryCodes.forEach { code ->
                                        DropdownMenuItem(
                                            text = { Text(code) },
                                            onClick = {
                                                countryCodePrimary = code
                                                expandedPrimary = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = phonePrimary,
                                onValueChange = { if (it.length <= 10) phonePrimary = it },
                                placeholder = { Text("9876543210") },
                                leadingIcon = { Icon(Icons.Outlined.Phone, null) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF8B5CF6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Secondary Phone with Country Code
                        Text(
                            text = "Secondary Phone (Optional)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            var expandedSecondary by remember { mutableStateOf(false) }
                            val countryCodes = listOf("+91", "+1", "+44", "+61", "+86")
                            
                            Box(modifier = Modifier.width(100.dp)) {
                                OutlinedTextField(
                                    value = countryCodeSecondary,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            Icons.Filled.ArrowDropDown,
                                            null,
                                            modifier = Modifier.clickable { expandedSecondary = !expandedSecondary }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF8B5CF6),
                                        unfocusedBorderColor = Color(0xFFD1D5DB)
                                    )
                                )
                                DropdownMenu(
                                    expanded = expandedSecondary,
                                    onDismissRequest = { expandedSecondary = false }
                                ) {
                                    countryCodes.forEach { code ->
                                        DropdownMenuItem(
                                            text = { Text(code) },
                                            onClick = {
                                                countryCodeSecondary = code
                                                expandedSecondary = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = phoneSecondary,
                                onValueChange = { if (it.length <= 10) phoneSecondary = it },
                                placeholder = { Text("9876543210") },
                                leadingIcon = { Icon(Icons.Outlined.Phone, null) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF8B5CF6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = specialization,
                            onValueChange = { specialization = it },
                            label = { Text("Specialization *") },
                            placeholder = { Text("Cardiology, Neurology, etc.") },
                            leadingIcon = { Icon(Icons.Outlined.MedicalServices, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = qualifications,
                            onValueChange = { qualifications = it },
                            label = { Text("Qualifications (comma-separated) *") },
                            placeholder = { Text("MBBS, MD, DM") },
                            leadingIcon = { Icon(Icons.Outlined.School, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = experienceYears,
                            onValueChange = { experienceYears = it },
                            label = { Text("Years of Experience *") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                    }
                    
                    3 -> {
                        Text(
                            "Additional Information (Optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = consultationFee,
                            onValueChange = { consultationFee = it },
                            label = { Text("Consultation Fee (₹)") },
                            leadingIcon = { Icon(Icons.Outlined.CurrencyRupee, null) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = availableForEmergency,
                                onCheckedChange = { availableForEmergency = it }
                            )
                            Text("Available for Emergency Cases")
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = telemedicineEnabled,
                                onCheckedChange = { telemedicineEnabled = it }
                            )
                            Text("Telemedicine Enabled")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = languages,
                            onValueChange = { languages = it },
                            label = { Text("Languages (comma-separated)") },
                            placeholder = { Text("English, Hindi, Tamil") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = clinicAddress,
                            onValueChange = { clinicAddress = it },
                            label = { Text("Clinic Address") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Professional Bio") },
                            placeholder = { Text("Brief description of your practice...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                    }
                }
            }
            
            // Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Filled.ArrowBack, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }
                }
                
                Button(
                    onClick = {
                        if (currentStep < totalSteps) {
                            // Check password match first for Step 1
                            if (currentStep == 1 && password != confirmPassword) {
                                passwordError = true
                                Toast.makeText(context, "Passwords don't match", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            val isValid = when (currentStep) {
                                1 -> email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() &&
                                        medicalLicenseNo.isNotBlank() && registrationYear.isNotBlank() &&
                                        registrationState.isNotBlank() && selectedHospitals.isNotEmpty()
                                2 -> title.isNotBlank() && firstName.isNotBlank() && lastName.isNotBlank() &&
                                        phonePrimary.isNotBlank() && specialization.isNotBlank() &&
                                        qualifications.isNotBlank() && experienceYears.isNotBlank()
                                else -> true
                            }
                            
                            if (isValid) {
                                currentStep++
                            } else {
                                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (password != confirmPassword) {
                                Toast.makeText(context, "Passwords don't match", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            if (selectedHospitals.isEmpty()) {
                                Toast.makeText(context, "Please select at least one hospital", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            val qualList = qualifications.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            val langList = languages.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { null }
                            
                            // Use the first hospital as primary for registration
                            val primaryHospitalCode = selectedHospitals.first().hospitalCode ?: "UNKNOWN"
                            
                            val request = RegisterDoctorRequest(
                                email = email,
                                password = password,
                                medicalLicenseNo = medicalLicenseNo,
                                registrationYear = registrationYear.toInt(),
                                registrationState = registrationState,
                                title = title,
                                firstName = firstName,
                                middleName = middleName.ifBlank { null },
                                lastName = lastName,
                                phonePrimary = "$countryCodePrimary$phonePrimary",
                                phoneSecondary = if (phoneSecondary.isNotBlank()) "$countryCodeSecondary$phoneSecondary" else null,
                                specialization = specialization,
                                qualifications = qualList,
                                experienceYears = experienceYears.toInt(),
                                hospitalCode = primaryHospitalCode,
                                consultationFee = consultationFee.toDoubleOrNull(),
                                availableForEmergency = availableForEmergency,
                                telemedicineEnabled = telemedicineEnabled,
                                languages = langList,
                                clinicAddress = clinicAddress.ifBlank { null },
                                bio = bio.ifBlank { null }
                            )
                            
                            authViewModel.registerDoctor(request)
                        }
                    },
                    modifier = Modifier.weight(if (currentStep > 1) 1f else 1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            if (currentStep < totalSteps) "Continue" else "Create Account",
                            fontWeight = FontWeight.SemiBold
                        )
                        if (currentStep < totalSteps) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HospitalDropdownItem(
    hospital: HospitalProfileResponse,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isSelected) Color(0xFFF0F9FF) else Color.Transparent)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                hospital.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    hospital.hospitalCode ?: "N/A",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8B5CF6),
                    fontWeight = FontWeight.SemiBold
                )
                if (!hospital.city.isNullOrBlank()) {
                    Text("•", color = Color(0xFF9CA3AF), style = MaterialTheme.typography.labelSmall)
                    Text(
                        "${hospital.city}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
        if (isSelected) {
            Icon(
                Icons.Filled.CheckCircle,
                null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                Icons.Outlined.AddCircle,
                null,
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
