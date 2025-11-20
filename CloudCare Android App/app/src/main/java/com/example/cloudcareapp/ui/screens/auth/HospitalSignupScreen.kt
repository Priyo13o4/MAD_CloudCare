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
fun HospitalSignupScreen(
    onBackClick: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    // Form state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var countryCodePrimary by remember { mutableStateOf("+91") }
    var phonePrimary by remember { mutableStateOf("") }
    var countryCodeSecondary by remember { mutableStateOf("+91") }
    var phoneSecondary by remember { mutableStateOf("") }
    var countryCodeEmergency by remember { mutableStateOf("+91") }
    var phoneEmergency by remember { mutableStateOf("") }
    var addressLine1 by remember { mutableStateOf("") }
    var addressLine2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var totalBeds by remember { mutableStateOf("") }
    var icuBeds by remember { mutableStateOf("") }
    var emergencyBeds by remember { mutableStateOf("") }
    var operationTheatres by remember { mutableStateOf("") }
    var hasEmergency by remember { mutableStateOf(true) }
    var hasAmbulance by remember { mutableStateOf(false) }
    var hasPharmacy by remember { mutableStateOf(false) }
    var hasLab by remember { mutableStateOf(false) }
    var hasBloodBank by remember { mutableStateOf(false) }
    var selectedFacilityType by remember { mutableStateOf<FacilityType?>(null) }
    var specializations by remember { mutableStateOf("") }
    var accreditations by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(1) }
    
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    
    val authState by authViewModel.authState.observeAsState()
    val isLoading by authViewModel.isLoading.observeAsState(false)
    
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthViewModel.AuthState.Success -> {
                Toast.makeText(context, "Hospital registered successfully!", Toast.LENGTH_SHORT).show()
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
                .background(Brush.verticalGradient(listOf(Color(0xFFF5F7FA), Color.White)))
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = Color(0xFF1A1A1A))
                }
                Text(
                    "Hospital Registration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            LinearProgressIndicator(
                progress = currentStep.toFloat() / totalSteps,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                color = Color(0xFF10B981),
                trackColor = Color(0xFFE5E7EB)
            )
            
            Text(
                "Step $currentStep of $totalSteps",
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
                        Text("Account & Basic Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = email, onValueChange = { email = it },
                            label = { Text("Email Address *") },
                            leadingIcon = { Icon(Icons.Outlined.Email, null) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = password, onValueChange = { password = it },
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
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = confirmPassword, onValueChange = { 
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
                                focusedBorderColor = Color(0xFF10B981), 
                                unfocusedBorderColor = if (passwordError) MaterialTheme.colorScheme.error else Color(0xFFD1D5DB),
                                errorBorderColor = MaterialTheme.colorScheme.error
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            label = { Text("Hospital/Facility Name *") },
                            leadingIcon = { Icon(Icons.Outlined.LocalHospital, null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text("Facility Type *", style = MaterialTheme.typography.labelMedium, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        var expandedFacilityType by remember { mutableStateOf(false) }
                        val facilityTypes = listOf(
                            FacilityType.MULTI_SPECIALTY_HOSPITAL to "Multi-Specialty Hospital",
                            FacilityType.SUPER_SPECIALTY_HOSPITAL to "Super-Specialty Hospital",
                            FacilityType.GENERAL_HOSPITAL to "General Hospital",
                            FacilityType.CLINIC to "Clinic",
                            FacilityType.DIAGNOSTIC_CENTER to "Diagnostic Center"
                        )
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedFacilityType?.let { type ->
                                    facilityTypes.find { it.first == type }?.second ?: ""
                                } ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Facility Type") },
                                trailingIcon = {
                                    Icon(
                                        if (expandedFacilityType) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                        null,
                                        modifier = Modifier.clickable { expandedFacilityType = !expandedFacilityType }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedFacilityType = !expandedFacilityType },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color(0xFFD1D5DB)
                                )
                            )
                            DropdownMenu(
                                expanded = expandedFacilityType,
                                onDismissRequest = { expandedFacilityType = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                facilityTypes.forEach { (type, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            selectedFacilityType = type
                                            expandedFacilityType = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    2 -> {
                        Text("Contact & Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Primary Phone with Country Code
                        Text(text = "Primary Phone *", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            var expandedPrimary by remember { mutableStateOf(false) }
                            val countryCodes = listOf("+91", "+1", "+44", "+61", "+86")
                            Box(modifier = Modifier.width(100.dp)) {
                                OutlinedTextField(value = countryCodePrimary, onValueChange = {}, readOnly = true,
                                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, modifier = Modifier.clickable { expandedPrimary = !expandedPrimary }) },
                                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                                DropdownMenu(expanded = expandedPrimary, onDismissRequest = { expandedPrimary = false }) {
                                    countryCodes.forEach { code -> DropdownMenuItem(text = { Text(code) }, onClick = { countryCodePrimary = code; expandedPrimary = false }) }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(value = phonePrimary, onValueChange = { if (it.length <= 10) phonePrimary = it }, placeholder = { Text("9876543210") },
                                leadingIcon = { Icon(Icons.Outlined.Phone, null) }, modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Secondary Phone with Country Code
                        Text(text = "Secondary Phone (Optional)", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            var expandedSecondary by remember { mutableStateOf(false) }
                            val countryCodes = listOf("+91", "+1", "+44", "+61", "+86")
                            Box(modifier = Modifier.width(100.dp)) {
                                OutlinedTextField(value = countryCodeSecondary, onValueChange = {}, readOnly = true,
                                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, modifier = Modifier.clickable { expandedSecondary = !expandedSecondary }) },
                                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                                DropdownMenu(expanded = expandedSecondary, onDismissRequest = { expandedSecondary = false }) {
                                    countryCodes.forEach { code -> DropdownMenuItem(text = { Text(code) }, onClick = { countryCodeSecondary = code; expandedSecondary = false }) }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(value = phoneSecondary, onValueChange = { if (it.length <= 10) phoneSecondary = it }, placeholder = { Text("9876543210") },
                                leadingIcon = { Icon(Icons.Outlined.Phone, null) }, modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Emergency Phone with Country Code
                        Text(text = "Emergency Phone (Optional)", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            var expandedEmergency by remember { mutableStateOf(false) }
                            val countryCodes = listOf("+91", "+1", "+44", "+61", "+86")
                            Box(modifier = Modifier.width(100.dp)) {
                                OutlinedTextField(value = countryCodeEmergency, onValueChange = {}, readOnly = true,
                                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, modifier = Modifier.clickable { expandedEmergency = !expandedEmergency }) },
                                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                                DropdownMenu(expanded = expandedEmergency, onDismissRequest = { expandedEmergency = false }) {
                                    countryCodes.forEach { code -> DropdownMenuItem(text = { Text(code) }, onClick = { countryCodeEmergency = code; expandedEmergency = false }) }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(value = phoneEmergency, onValueChange = { if (it.length <= 10) phoneEmergency = it }, placeholder = { Text("9876543210") },
                                leadingIcon = { Icon(Icons.Outlined.EmergencyShare, null) }, modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(value = addressLine1, onValueChange = { addressLine1 = it }, label = { Text("Address Line 1 *") },
                            leadingIcon = { Icon(Icons.Outlined.Home, null) }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(value = addressLine2, onValueChange = { addressLine2 = it }, label = { Text("Address Line 2") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City *") },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State *") },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(value = postalCode, onValueChange = { postalCode = it }, label = { Text("Postal Code *") },
                            leadingIcon = { Icon(Icons.Outlined.PinDrop, null) }, modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                    }
                    
                    3 -> {
                        Text("Facility Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(value = totalBeds, onValueChange = { totalBeds = it }, label = { Text("Total Beds *") },
                                modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(value = icuBeds, onValueChange = { icuBeds = it }, label = { Text("ICU Beds *") },
                                modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(value = emergencyBeds, onValueChange = { emergencyBeds = it }, label = { Text("Emergency Beds *") },
                                modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(value = operationTheatres, onValueChange = { operationTheatres = it }, label = { Text("OTs *") },
                                modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Facilities & Services", style = MaterialTheme.typography.labelMedium, color = Color(0xFF6B7280))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = hasEmergency, onCheckedChange = { hasEmergency = it })
                            Text("24/7 Emergency Services")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = hasAmbulance, onCheckedChange = { hasAmbulance = it })
                            Text("Ambulance Services")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = hasPharmacy, onCheckedChange = { hasPharmacy = it })
                            Text("In-house Pharmacy")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = hasLab, onCheckedChange = { hasLab = it })
                            Text("Diagnostic Laboratory")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = hasBloodBank, onCheckedChange = { hasBloodBank = it })
                            Text("Blood Bank")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(value = specializations, onValueChange = { specializations = it },
                            label = { Text("Specializations (comma-separated)") },
                            placeholder = { Text("Cardiology, Neurology, Orthopedics") },
                            modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(value = accreditations, onValueChange = { accreditations = it },
                            label = { Text("Accreditations (comma-separated)") },
                            placeholder = { Text("NABH, JCI, ISO 9001") },
                            modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981), unfocusedBorderColor = Color(0xFFD1D5DB)))
                    }
                }
            }
            
            // Navigation Buttons
            Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (currentStep > 1) {
                    OutlinedButton(onClick = { currentStep-- }, modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp), enabled = !isLoading) {
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
                                        name.isNotBlank() && selectedFacilityType != null
                                2 -> phonePrimary.isNotBlank() && addressLine1.isNotBlank() && city.isNotBlank() &&
                                        state.isNotBlank() && postalCode.isNotBlank()
                                3 -> totalBeds.isNotBlank() && icuBeds.isNotBlank() && emergencyBeds.isNotBlank() &&
                                        operationTheatres.isNotBlank()
                                else -> true
                            }
                            if (isValid) currentStep++ else Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                        } else {
                            if (password != confirmPassword) {
                                passwordError = true
                                Toast.makeText(context, "Passwords don't match", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            val specList = specializations.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { null }
                            val accList = accreditations.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty { null }
                            
                            val request = RegisterHospitalRequest(
                                email = email, password = password, name = name,
                                phonePrimary = "$countryCodePrimary$phonePrimary", 
                                phoneSecondary = if (phoneSecondary.isNotBlank()) "$countryCodeSecondary$phoneSecondary" else null,
                                phoneEmergency = if (phoneEmergency.isNotBlank()) "$countryCodeEmergency$phoneEmergency" else null,
                                addressLine1 = addressLine1, addressLine2 = addressLine2.ifBlank { null },
                                city = city, state = state, postalCode = postalCode,
                                totalBeds = totalBeds.toInt(), icuBeds = icuBeds.toInt(),
                                emergencyBeds = emergencyBeds.toInt(), operationTheatres = operationTheatres.toInt(),
                                hasEmergency = hasEmergency, hasAmbulance = hasAmbulance,
                                hasPharmacy = hasPharmacy, hasLab = hasLab, hasBloodBank = hasBloodBank,
                                facilityType = selectedFacilityType!!,
                                specializations = specList, accreditations = accList
                            )
                            
                            authViewModel.registerHospital(request)
                        }
                    },
                    modifier = Modifier.weight(if (currentStep > 1) 1f else 1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp), enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(if (currentStep < totalSteps) "Continue" else "Create Account", fontWeight = FontWeight.SemiBold)
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
