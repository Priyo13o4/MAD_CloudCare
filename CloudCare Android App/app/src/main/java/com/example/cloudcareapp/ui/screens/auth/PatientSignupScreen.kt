package com.example.cloudcareapp.ui.screens.auth

import android.app.DatePickerDialog
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cloudcareapp.data.model.*
import com.example.cloudcareapp.ui.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientSignupScreen(
    onBackClick: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    // Form state - Personal
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var aadharNumber by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") } // Display format: DD-MM-YYYY
    var dateOfBirthISO by remember { mutableStateOf("") } // Backend format: YYYY-MM-DD
    var selectedGender by remember { mutableStateOf<Gender?>(null) }
    var selectedBloodGroup by remember { mutableStateOf<BloodGroup?>(null) }
    
    // Contact
    var countryCodePrimary by remember { mutableStateOf("+91") }
    var phonePrimary by remember { mutableStateOf("") }
    var countryCodeSecondary by remember { mutableStateOf("+91") }
    var phoneSecondary by remember { mutableStateOf("") }
    
    // Address
    var addressLine1 by remember { mutableStateOf("") }
    var addressLine2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    
    // Emergency Contact
    var emergencyContactName by remember { mutableStateOf("") }
    var countryCodeEmergency by remember { mutableStateOf("+91") }
    var emergencyContactPhone by remember { mutableStateOf("") }
    var emergencyContactRelation by remember { mutableStateOf("") }
    
    // Medical (Optional)
    var heightCm by remember { mutableStateOf("") }
    var weightKg by remember { mutableStateOf("") }
    
    // Insurance (Optional)
    var insuranceProvider by remember { mutableStateOf("") }
    var insurancePolicyNo by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var aadharError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(1) }
    
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    
    val authState by authViewModel.authState.observeAsState()
    val isLoading by authViewModel.isLoading.observeAsState(false)
    val errorMessage by authViewModel.errorMessage.observeAsState()
    
    // Handle auth state
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
    
    val totalSteps = 4
    
    // DatePickerDialog
    LaunchedEffect(showDatePicker) {
        if (showDatePicker) {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            
            DatePickerDialog(
                context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Display format: DD-MM-YYYY
                    dateOfBirth = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
                    // ISO format for backend: YYYY-MM-DD
                    dateOfBirthISO = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    showDatePicker = false
                },
                year - 18, // Default to 18 years ago
                month,
                day
            ).apply {
                datePicker.maxDate = System.currentTimeMillis() // Can't select future dates
                setOnCancelListener { showDatePicker = false }
            }.show()
        }
    }
    
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
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1A1A1A)
                    )
                }
                Text(
                    text = "Patient Registration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            // Progress Indicator
            LinearProgressIndicator(
                progress = currentStep.toFloat() / totalSteps,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                color = Color(0xFF3B82F6),
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
                        // Step 1: Account & Personal Info
                        Text(
                            text = "Account & Personal Information",
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
                                focusedBorderColor = Color(0xFF3B82F6),
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
                                    Icon(
                                        if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                        null
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { 
                                confirmPassword = it
                                passwordError = false // Clear error on typing
                            },
                            label = { Text("Confirm Password *") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        if (confirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                        null
                                    )
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
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = if (passwordError) MaterialTheme.colorScheme.error else Color(0xFFD1D5DB),
                                errorBorderColor = MaterialTheme.colorScheme.error
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = aadharNumber,
                            onValueChange = { 
                                if (it.length <= 12 && it.all { char -> char.isDigit() }) {
                                    aadharNumber = it
                                    aadharError = false // Clear error on typing
                                }
                            },
                            label = { Text("Aadhar Number (12 digits) *") },
                            leadingIcon = { Icon(Icons.Outlined.Badge, null) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            isError = aadharError,
                            supportingText = if (aadharError) {
                                { Text("Aadhar must be exactly 12 digits", color = MaterialTheme.colorScheme.error) }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = if (aadharError) MaterialTheme.colorScheme.error else Color(0xFFD1D5DB),
                                errorBorderColor = MaterialTheme.colorScheme.error
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { Text("First Name *") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = middleName,
                                onValueChange = { middleName = it },
                                label = { Text("Middle") },
                                modifier = Modifier.weight(0.7f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Last Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true }
                        ) {
                            OutlinedTextField(
                                value = dateOfBirth,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Date of Birth (DD-MM-YYYY) *") },
                                placeholder = { Text("DD-MM-YYYY") },
                                leadingIcon = { Icon(Icons.Outlined.CalendarToday, null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                ),
                                enabled = false
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Gender *",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF6B7280)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Gender.values().forEach { gender ->
                                FilterChip(
                                    selected = selectedGender == gender,
                                    onClick = { selectedGender = gender },
                                    label = { Text(gender.name) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    2 -> {
                        // Step 2: Contact & Address
                        Text(
                            text = "Contact & Address Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
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
                                        focusedBorderColor = Color(0xFF3B82F6),
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
                                    focusedBorderColor = Color(0xFF3B82F6),
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
                                        focusedBorderColor = Color(0xFF3B82F6),
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
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = addressLine1,
                            onValueChange = { addressLine1 = it },
                            label = { Text("Address Line 1 *") },
                            leadingIcon = { Icon(Icons.Outlined.Home, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = addressLine2,
                            onValueChange = { addressLine2 = it },
                            label = { Text("Address Line 2 (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City *") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = state,
                                onValueChange = { state = it },
                                label = { Text("State *") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = postalCode,
                            onValueChange = { postalCode = it },
                            label = { Text("Postal Code *") },
                            leadingIcon = { Icon(Icons.Outlined.PinDrop, null) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                    }
                    
                    3 -> {
                        // Step 3: Emergency Contact
                        Text(
                            text = "Emergency Contact Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = emergencyContactName,
                            onValueChange = { emergencyContactName = it },
                            label = { Text("Emergency Contact Name *") },
                            leadingIcon = { Icon(Icons.Outlined.Person, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Emergency Contact Phone with Country Code
                        Text(
                            text = "Emergency Contact Phone *",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            var expandedEmergency by remember { mutableStateOf(false) }
                            val countryCodes = listOf("+91", "+1", "+44", "+61", "+86")
                            
                            Box(modifier = Modifier.width(100.dp)) {
                                OutlinedTextField(
                                    value = countryCodeEmergency,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            Icons.Filled.ArrowDropDown,
                                            null,
                                            modifier = Modifier.clickable { expandedEmergency = !expandedEmergency }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF3B82F6),
                                        unfocusedBorderColor = Color(0xFFD1D5DB)
                                    )
                                )
                                DropdownMenu(
                                    expanded = expandedEmergency,
                                    onDismissRequest = { expandedEmergency = false }
                                ) {
                                    countryCodes.forEach { code ->
                                        DropdownMenuItem(
                                            text = { Text(code) },
                                            onClick = {
                                                countryCodeEmergency = code
                                                expandedEmergency = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = emergencyContactPhone,
                                onValueChange = { if (it.length <= 10) emergencyContactPhone = it },
                                placeholder = { Text("9876543210") },
                                leadingIcon = { Icon(Icons.Outlined.Phone, null) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = emergencyContactRelation,
                            onValueChange = { emergencyContactRelation = it },
                            label = { Text("Relationship *") },
                            placeholder = { Text("Spouse/Parent/Sibling") },
                            leadingIcon = { Icon(Icons.Outlined.FamilyRestroom, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Medical Information (Optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Blood Group",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF6B7280)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("A+", "A-", "B+", "B-").forEach { bg ->
                                FilterChip(
                                    selected = selectedBloodGroup?.name == bg.replace("+", "_POSITIVE").replace("-", "_NEGATIVE"),
                                    onClick = {
                                        selectedBloodGroup = BloodGroup.valueOf(bg.replace("+", "_POSITIVE").replace("-", "_NEGATIVE"))
                                    },
                                    label = { Text(bg) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("AB+", "AB-", "O+", "O-").forEach { bg ->
                                FilterChip(
                                    selected = selectedBloodGroup?.name == bg.replace("+", "_POSITIVE").replace("-", "_NEGATIVE"),
                                    onClick = {
                                        selectedBloodGroup = BloodGroup.valueOf(bg.replace("+", "_POSITIVE").replace("-", "_NEGATIVE"))
                                    },
                                    label = { Text(bg) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = heightCm,
                                onValueChange = { heightCm = it },
                                label = { Text("Height (cm)") },
                                leadingIcon = { Icon(Icons.Outlined.Height, null) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = weightKg,
                                onValueChange = { weightKg = it },
                                label = { Text("Weight (kg)") },
                                leadingIcon = { Icon(Icons.Outlined.MonitorWeight, null) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                        }
                    }
                    
                    4 -> {
                        // Step 4: Insurance (Optional)
                        Text(
                            text = "Insurance Information (Optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You can skip this step and add insurance details later from your profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = insuranceProvider,
                            onValueChange = { insuranceProvider = it },
                            label = { Text("Insurance Provider") },
                            placeholder = { Text("Star Health, HDFC Ergo, etc.") },
                            leadingIcon = { Icon(Icons.Outlined.HealthAndSafety, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = insurancePolicyNo,
                            onValueChange = { insurancePolicyNo = it },
                            label = { Text("Policy Number") },
                            leadingIcon = { Icon(Icons.Outlined.Numbers, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFFD1D5DB)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFEFF6FF)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF3B82F6)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Review Your Information",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A1A1A)
                                    )
                                    Text(
                                        text = "Make sure all required fields are filled correctly before submitting",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                        }
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
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }
                }
                
                Button(
                    onClick = {
                        if (currentStep < totalSteps) {
                            // Validate current step
                            if (currentStep == 1) {
                                // Validate passwords match
                                if (password != confirmPassword) {
                                    passwordError = true
                                    Toast.makeText(context, "Passwords don't match", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                
                                // Validate Aadhar length
                                if (aadharNumber.length != 12) {
                                    aadharError = true
                                    Toast.makeText(context, "Aadhar must be exactly 12 digits", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                            }
                            
                            val isValid = when (currentStep) {
                                1 -> email.isNotBlank() && password.isNotBlank() && password == confirmPassword &&
                                        aadharNumber.length == 12 && firstName.isNotBlank() && lastName.isNotBlank() &&
                                        dateOfBirth.isNotBlank() && selectedGender != null
                                2 -> phonePrimary.isNotBlank() && addressLine1.isNotBlank() &&
                                        city.isNotBlank() && state.isNotBlank() && postalCode.isNotBlank()
                                3 -> emergencyContactName.isNotBlank() && emergencyContactPhone.isNotBlank() &&
                                        emergencyContactRelation.isNotBlank()
                                else -> true
                            }
                            
                            if (isValid) {
                                passwordError = false
                                aadharError = false
                                currentStep++
                            } else {
                                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Final validation and submit
                            if (password != confirmPassword) {
                                passwordError = true
                                Toast.makeText(context, "Passwords don't match", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            if (aadharNumber.length != 12) {
                                aadharError = true
                                Toast.makeText(context, "Aadhar must be exactly 12 digits", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            val request = RegisterPatientRequest(
                                email = email,
                                password = password,
                                aadharNumber = aadharNumber,
                                firstName = firstName,
                                middleName = middleName.ifBlank { null },
                                lastName = lastName,
                                dateOfBirth = dateOfBirthISO,
                                gender = selectedGender!!,
                                bloodGroup = selectedBloodGroup,
                                phonePrimary = "$countryCodePrimary$phonePrimary",
                                phoneSecondary = if (phoneSecondary.isNotBlank()) "$countryCodeSecondary$phoneSecondary" else null,
                                addressLine1 = addressLine1,
                                addressLine2 = addressLine2.ifBlank { null },
                                city = city,
                                state = state,
                                postalCode = postalCode,
                                emergencyContactName = emergencyContactName,
                                emergencyContactPhone = "$countryCodeEmergency$emergencyContactPhone",
                                emergencyContactRelation = emergencyContactRelation,
                                heightCm = heightCm.toDoubleOrNull(),
                                weightKg = weightKg.toDoubleOrNull(),
                                insuranceProvider = insuranceProvider.ifBlank { null },
                                insurancePolicyNo = insurancePolicyNo.ifBlank { null }
                            )
                            
                            authViewModel.registerPatient(request)
                        }
                    },
                    modifier = Modifier
                        .weight(if (currentStep > 1) 1f else 1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    )
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
                            Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}
