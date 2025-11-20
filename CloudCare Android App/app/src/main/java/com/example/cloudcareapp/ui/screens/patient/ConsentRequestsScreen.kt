package com.example.cloudcareapp.ui.screens.patient

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.cloudcareapp.data.model.ConsentResponse
import com.example.cloudcareapp.data.model.UpdateConsentRequest
import com.example.cloudcareapp.data.remote.RetrofitClient
import com.example.cloudcareapp.ui.components.CommonTopAppBar
import com.example.cloudcareapp.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Consent Requests Screen for Patients
 * Shows all consent requests from doctors
 * Allows approve/deny actions
 * 
 * Note: No Scaffold/TopAppBar here since this is used as a bottom nav destination.
 * The main layout already provides the top bar.
 */
@Composable
fun ConsentRequestsScreen(
    onBackClick: () -> Unit = {}  // Not used, kept for compatibility
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var consentRequests by remember { mutableStateOf<List<ConsentResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("PENDING") }
    
    val patientId = remember { AppDataCache.getPatientId() }
    
    // Load consent requests
    fun loadConsents(filter: String? = null) {
        if (patientId == null) {
            errorMessage = "Patient ID not found"
            isLoading = false
            return
        }
        
        isLoading = true
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val requests = RetrofitClient.apiService.getPatientConsents(
                        patientId = patientId,
                        statusFilter = filter
                    )
                    
                    withContext(Dispatchers.Main) {
                        consentRequests = requests
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = e.message ?: "Failed to load consent requests"
                    isLoading = false
                }
            }
        }
    }
    
    // Load on first composition
    LaunchedEffect(selectedFilter) {
        loadConsents(selectedFilter)
    }
    
    // ✅ FIX: Removed Scaffold and TopAppBar to prevent duplication with main layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Filter tabs
        Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "PENDING",
                        onClick = { selectedFilter = "PENDING" },
                        label = { Text("Pending") },
                        leadingIcon = {
                            if (selectedFilter == "PENDING") {
                                Icon(Icons.Filled.HourglassEmpty, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                    
                    FilterChip(
                        selected = selectedFilter == "APPROVED",
                        onClick = { selectedFilter = "APPROVED" },
                        label = { Text("Approved") },
                        leadingIcon = {
                            if (selectedFilter == "APPROVED") {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                    
                    FilterChip(
                        selected = selectedFilter == "DENIED",
                        onClick = { selectedFilter = "DENIED" },
                        label = { Text("Denied") },
                        leadingIcon = {
                            if (selectedFilter == "DENIED") {
                                Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )

                    FilterChip(
                        selected = selectedFilter == "REVOKED",
                        onClick = { selectedFilter = "REVOKED" },
                        label = { Text("Revoked") },
                        leadingIcon = {
                            if (selectedFilter == "REVOKED") {
                                Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                }
            }
            
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                consentRequests.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Inbox,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No ${selectedFilter.lowercase()} consent requests",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(consentRequests) { consent ->
                            ConsentRequestCard(
                                consent = consent,
                                onApprove = {
                                    scope.launch {
                                        try {
                                            withContext(Dispatchers.IO) {
                                                RetrofitClient.apiService.updateConsentStatus(
                                                    consentId = consent.id,
                                                    request = UpdateConsentRequest("APPROVED")
                                                )
                                            }
                                            
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Consent approved! Doctor can now access your data.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                loadConsents(selectedFilter)
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to approve: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                },
                                onDeny = {
                                    scope.launch {
                                        try {
                                            withContext(Dispatchers.IO) {
                                                RetrofitClient.apiService.updateConsentStatus(
                                                    consentId = consent.id,
                                                    request = UpdateConsentRequest("DENIED")
                                                )
                                            }
                                            
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Consent denied",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                loadConsents(selectedFilter)
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to deny: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                },
                                onRevoke = {
                                    scope.launch {
                                        try {
                                            withContext(Dispatchers.IO) {
                                                RetrofitClient.apiService.updateConsentStatus(
                                                    consentId = consent.id,
                                                    request = UpdateConsentRequest("REVOKED")
                                                )
                                            }
                                            
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Consent revoked",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                loadConsents(selectedFilter)
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to revoke: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
private fun ConsentRequestCard(
    consent: ConsentResponse,
    onApprove: () -> Unit,
    onDeny: () -> Unit,
    onRevoke: () -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()) }
    val requestedDate = remember(consent.requestedAt) {
        try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = isoFormat.parse(consent.requestedAt)
            date?.let { dateFormat.format(it) } ?: consent.requestedAt
        } catch (e: Exception) {
            consent.requestedAt
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalHospital,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Text(
                        text = consent.facilityName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                // Status badge
                StatusBadge(status = consent.status)
            }
            
            // Description
            if (consent.description != null) {
                Text(
                    text = consent.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            
            // Request type
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = consent.requestType,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Date
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Requested: $requestedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
            
            // Action buttons (only for PENDING status)
            if (consent.status == "PENDING") {
                Divider()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDeny,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Deny")
                    }
                    
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve")
                    }
                }
            } else if (consent.status == "APPROVED") {
                Divider()
                
                Button(
                    onClick = { 
                        // We reuse onDeny for revoke since it sets status to DENIED/REVOKED
                        // But wait, onDeny sets it to DENIED. We need REVOKED.
                        // The parent component passes a lambda that sets status to DENIED.
                        // We need to change the parent to handle REVOKED or just use DENIED for now?
                        // The user asked for "revoke".
                        // Let's assume we need a separate callback or modify the existing one.
                        // For now, I'll use a separate callback if I can, but I can't change the signature easily without changing the caller.
                        // Actually, I can change the caller in the same file.
                        onRevoke()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Revoke Access")
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (backgroundColor, textColor, icon) = when (status) {
        "PENDING" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), Icons.Filled.HourglassEmpty)
        "APPROVED" -> Triple(Color(0xFFD1FAE5), Color(0xFF059669), Icons.Filled.CheckCircle)
        "DENIED" -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), Icons.Filled.Cancel)
        "REVOKED" -> Triple(Color(0xFFF3F4F6), Color(0xFF6B7280), Icons.Filled.History)
        else -> Triple(Color(0xFFF3F4F6), Color(0xFF6B7280), Icons.Filled.Help)
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
