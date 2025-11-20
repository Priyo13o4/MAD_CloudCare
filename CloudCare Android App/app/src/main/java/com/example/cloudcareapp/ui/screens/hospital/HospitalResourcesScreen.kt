package com.example.cloudcareapp.ui.screens.hospital

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.data.cache.AppDataCache
import com.example.cloudcareapp.data.model.ResourceUpdate
import com.example.cloudcareapp.data.repository.HospitalRepository
import com.example.cloudcareapp.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun IntegerInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var isError by remember(value) { mutableStateOf(value.isNotEmpty() && !value.all { it.isDigit() }) }

    OutlinedTextField(
        value = value,
        onValueChange = { 
            // Disallow new lines and strictly enforce digits if we want to prevent entry,
            // but requirement says "Show Red outline... on invalid input", so maybe allow entry and show error?
            // "Disallow new lines (\n) or string characters." -> This sounds like prevention.
            // "Show Red outline... on invalid input" -> This sounds like validation.
            // I will prevent newlines, but allow other chars to show error if user manages to type them.
            val filtered = it.replace("\n", "")
            onValueChange(filtered)
            isError = filtered.isNotEmpty() && !filtered.all { char -> char.isDigit() }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        isError = isError,
        supportingText = {
            if (isError) {
                Text("Must be a valid number", color = Error)
            }
        },
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalResourcesScreen(
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { HospitalRepository() }
    
    var totalBeds by remember { mutableStateOf("") }
    var availableBeds by remember { mutableStateOf("") }
    var icuBeds by remember { mutableStateOf("") }
    var emergencyBeds by remember { mutableStateOf("") }
    
    // New Resources
    var oxygenCylinders by remember { mutableStateOf("") }
    var ventilators by remember { mutableStateOf("") }
    var ambulances by remember { mutableStateOf("") }
    var bloodBags by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val hospitalId = AppDataCache.getHospitalId()
        if (hospitalId != null) {
            scope.launch {
                try {
                    val profile = repository.getHospitalProfile(hospitalId)
                    totalBeds = profile.totalBeds.toString()
                    availableBeds = profile.availableBeds.toString()
                    icuBeds = profile.icuBeds.toString()
                    emergencyBeds = profile.emergencyBeds.toString()
                    oxygenCylinders = profile.oxygenCylinders.toString()
                    ventilators = profile.ventilators.toString()
                    ambulances = profile.ambulances.toString()
                    bloodBags = profile.bloodBags.toString()
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

    fun saveResources() {
        val hospitalId = AppDataCache.getHospitalId() ?: return
        
        // Validate all inputs
        val inputs = listOf(totalBeds, availableBeds, icuBeds, emergencyBeds, oxygenCylinders, ventilators, ambulances, bloodBags)
        if (inputs.any { it.isNotEmpty() && !it.all { char -> char.isDigit() } }) {
            error = "Please correct invalid inputs"
            return
        }

        isSaving = true
        error = null
        successMessage = null
        
        scope.launch {
            try {
                val update = ResourceUpdate(
                    totalBeds = totalBeds.toIntOrNull(),
                    availableBeds = availableBeds.toIntOrNull(),
                    icuBeds = icuBeds.toIntOrNull(),
                    emergencyBeds = emergencyBeds.toIntOrNull(),
                    oxygenCylinders = oxygenCylinders.toIntOrNull(),
                    ventilators = ventilators.toIntOrNull(),
                    ambulances = ambulances.toIntOrNull(),
                    bloodBags = bloodBags.toIntOrNull()
                )
                repository.updateResources(hospitalId, update)
                successMessage = "Resources updated successfully"
                isSaving = false
            } catch (e: Exception) {
                error = "Failed to update: ${e.message}"
                isSaving = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hospital Resources") },
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
        containerColor = Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Success
                )
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    "Bed Management",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IntegerInput(
                                        value = totalBeds,
                                        onValueChange = { totalBeds = it },
                                        label = "Total Beds",
                                        modifier = Modifier.weight(1f)
                                    )
                                    IntegerInput(
                                        value = availableBeds,
                                        onValueChange = { availableBeds = it },
                                        label = "Available",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IntegerInput(
                                        value = icuBeds,
                                        onValueChange = { icuBeds = it },
                                        label = "ICU Beds",
                                        modifier = Modifier.weight(1f)
                                    )
                                    IntegerInput(
                                        value = emergencyBeds,
                                        onValueChange = { emergencyBeds = it },
                                        label = "Emergency",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    "Medical Supplies & Equipment",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IntegerInput(
                                        value = oxygenCylinders,
                                        onValueChange = { oxygenCylinders = it },
                                        label = "Oxygen Cylinders",
                                        modifier = Modifier.weight(1f)
                                    )
                                    IntegerInput(
                                        value = ventilators,
                                        onValueChange = { ventilators = it },
                                        label = "Ventilators",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IntegerInput(
                                        value = ambulances,
                                        onValueChange = { ambulances = it },
                                        label = "Ambulances",
                                        modifier = Modifier.weight(1f)
                                    )
                                    IntegerInput(
                                        value = bloodBags,
                                        onValueChange = { bloodBags = it },
                                        label = "Blood Bags",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    
                    item {
                        if (error != null) {
                            Text(error!!, color = Error)
                        }
                        
                        if (successMessage != null) {
                            Text(successMessage!!, color = Success)
                        }
                        
                        Button(
                            onClick = { saveResources() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Success),
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Update Resources")
                            }
                        }
                    }
                }
            }
        }
    }
}
