package com.example.cloudcareapp.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.cloudcareapp.ui.viewmodel.AuthViewModel

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
    var hospitalCode by remember { mutableStateOf("") }
    var consultationFee by remember { mutableStateOf("") }
    var availableForEmergency by remember { mutableStateOf(false) }
    var telemedicineEnabled by remember { mutableStateOf(false) }
    var languages by remember { mutableStateOf("") }
    var clinicAddress by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(1) }
    
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    
    val authState by authViewModel.authState.observeAsState()
    val isLoading by authViewModel.isLoading.observeAsState(false)
    val errorMessage by authViewModel.errorMessage.observeAsState()
    
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
                        
                        OutlinedTextField(
                            value = hospitalCode,
                            onValueChange = { hospitalCode = it },
                            label = { Text("Hospital Code (HC-XXXXXX) *") },
                            placeholder = { Text("HC-ABC123") },
                            leadingIcon = { Icon(Icons.Outlined.LocalHospital, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
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
                                        registrationState.isNotBlank() && hospitalCode.isNotBlank()
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
                            
                            val qualList = qualifications.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            val langList = languages.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { null }
                            
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
                                hospitalCode = hospitalCode,
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
