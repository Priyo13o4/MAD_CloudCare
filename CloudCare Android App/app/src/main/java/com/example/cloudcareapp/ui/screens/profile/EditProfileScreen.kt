package com.example.cloudcareapp.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(true) }
    
    // Editable fields (non-core info)
    var phone by remember { mutableStateOf("+91 98765 43210") }
    var email by remember { mutableStateOf("patient7@cloudcare.local") }
    var address by remember { mutableStateOf("123 Medical Street, Bangalore, Karnataka") }
    var familyContact by remember { mutableStateOf("+91 98765 43211 (Father)") }
    var occupation by remember { mutableStateOf("Software Engineer") }
    
    var isSaving by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (isEditing) {
                                // Save changes
                                isSaving = true
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    isSaving = false
                                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                    onSaveSuccess()
                                }, 1000)
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Text(if (isSaving) "Saving..." else "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
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
        ) {
            // Non-editable info header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Primary.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Core Information (Cannot be edited)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow("Name", "Mobile Test User")
                    InfoRow("Patient ID", "7")
                    InfoRow("Age", "28 years")
                    InfoRow("Gender", "Male")
                    InfoRow("Blood Type", "O+")
                }
            }
            
            // Editable Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Contact Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    leadingIcon = {
                        Icon(Icons.Filled.Phone, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isEditing && !isSaving
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = {
                        Icon(Icons.Filled.Email, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isEditing && !isSaving
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    leadingIcon = {
                        Icon(Icons.Filled.Home, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    enabled = isEditing && !isSaving
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = familyContact,
                    onValueChange = { familyContact = it },
                    label = { Text("Emergency Contact") },
                    leadingIcon = {
                        Icon(Icons.Filled.ContactEmergency, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isEditing && !isSaving
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = occupation,
                    onValueChange = { occupation = it },
                    label = { Text("Occupation") },
                    leadingIcon = {
                        Icon(Icons.Filled.Work, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isEditing && !isSaving
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Save Button (alternative to top bar button)
                Button(
                    onClick = {
                        isSaving = true
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isSaving = false
                            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                            onSaveSuccess()
                        }, 1000)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Changes", fontWeight = FontWeight.SemiBold)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}
