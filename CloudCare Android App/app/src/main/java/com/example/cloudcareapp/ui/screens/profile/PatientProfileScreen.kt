package com.example.cloudcareapp.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.cloudcareapp.data.model.PatientProfileData
import com.example.cloudcareapp.ui.theme.*
import com.example.cloudcareapp.ui.viewmodel.AuthViewModel
import com.example.cloudcareapp.ui.viewmodel.PatientProfileViewModel

/**
 * Patient Profile Screen
 * Matches schema.prisma Patient model exactly
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val profileViewModel: PatientProfileViewModel = viewModel()
    
    val userSession by authViewModel.userSession.observeAsState()
    val profileState by profileViewModel.profileState.observeAsState()
    val isLoading by profileViewModel.isLoading.observeAsState(false)
    
    // Load profile when screen opens
    LaunchedEffect(userSession) {
        userSession?.user?.patientId?.let { patientId ->
            // Only load if we don't have cached data
            if (!profileViewModel.hasCache()) {
                profileViewModel.loadProfile(patientId)
            } else {
                // Load from cache immediately
                profileViewModel.loadProfile(patientId, forceRefresh = false)
            }
        }
    }
    
    // Handle profile state
    LaunchedEffect(profileState) {
        when (val state = profileState) {
            is PatientProfileViewModel.ProfileState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }
    
    // Extract patient data from state
    val patientData = when (val state = profileState) {
        is PatientProfileViewModel.ProfileState.Success -> state.profile
        else -> null
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            userSession?.user?.patientId?.let { patientId ->
                                profileViewModel.refreshProfile(patientId)
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
        ) {
            when {
                isLoading && patientData == null -> {
                    // Show loading spinner only on first load
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                patientData != null -> {
                    // Show profile content
                    ProfileContent(
                        patientData = patientData,
                        isRefreshing = isLoading
                    )
                }
                else -> {
                    // Show error state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = Error,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "Failed to load profile",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Button(
                                onClick = {
                                    userSession?.user?.patientId?.let { patientId ->
                                        profileViewModel.loadProfile(patientId, forceRefresh = true)
                                    }
                                }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    patientData: PatientProfileData,
    isRefreshing: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Show refresh indicator at top if refreshing
        if (isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Primary
            )
        }
        // Profile Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
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
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = patientData.name.take(1).uppercase(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = patientData.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Text(
                    text = "Patient ID: ${patientData.id.takeLast(6)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (patientData.age > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${patientData.age} years",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    if (patientData.gender.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = patientData.gender,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    if (patientData.bloodType.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Error.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = patientData.bloodType,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                if (patientData.emergency) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Error.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalHospital,
                                contentDescription = null,
                                tint = Error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Emergency Contact",
                                style = MaterialTheme.typography.labelMedium,
                                color = Error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
            
        // Personal Information
        PatientSectionCard(title = "Personal Information") {
            PatientInfoRow(
                icon = Icons.Filled.Badge,
                label = "Aadhar UID",
                value = if (patientData.aadharUid.isNotEmpty()) {
                    if (patientData.aadharUid.length > 14) {
                        "Linked (Hidden)"
                    } else {
                        // Format: XXXX XXXX XXXX
                        patientData.aadharUid.chunked(4).joinToString(" ")
                    }
                } else {
                    "Not linked"
                }
            )
            if (patientData.occupation != null) {
                PatientInfoRow(
                    icon = Icons.Filled.Work,
                    label = "Occupation",
                    value = patientData.occupation
                )
            }
        }
        
        // Contact Information
        PatientSectionCard(title = "Contact Information") {
            PatientInfoRow(
                icon = Icons.Filled.Email,
                label = "Email",
                value = patientData.email
            )
            PatientInfoRow(
                icon = Icons.Filled.Phone,
                label = "Phone",
                value = patientData.contact
            )
            PatientInfoRow(
                icon = Icons.Filled.Home,
                label = "Address",
                value = patientData.address
            )
            PatientInfoRow(
                icon = Icons.Filled.ContactPhone,
                label = "Emergency Contact",
                value = patientData.familyContact
            )
        }
        
        // Insurance Information (only show if has insurance)
        if (patientData.insuranceProvider != null || patientData.insuranceId != null) {
            PatientSectionCard(title = "Insurance Information") {
                if (patientData.insuranceProvider != null) {
                    PatientInfoRow(
                        icon = Icons.Filled.HealthAndSafety,
                        label = "Insurance Provider",
                        value = patientData.insuranceProvider
                    )
                }
                if (patientData.insuranceId != null) {
                    PatientInfoRow(
                        icon = Icons.Filled.CardMembership,
                        label = "Policy Number",
                        value = patientData.insuranceId
                    )
                }
            }
        }
        
        // AI Analysis (if available)
        if (!patientData.aiAnalysis.isNullOrEmpty()) {
            PatientSectionCard(title = "AI Health Analysis") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.Psychology,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = patientData.aiAnalysis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PatientSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
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
                color = TextPrimary
            )
            content()
        }
    }
}

@Composable
private fun PatientInfoRow(
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
            tint = Primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
