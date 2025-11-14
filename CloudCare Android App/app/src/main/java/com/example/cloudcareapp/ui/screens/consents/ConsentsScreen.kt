package com.example.cloudcareapp.ui.screens.consents

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cloudcareapp.data.model.*
import com.example.cloudcareapp.ui.theme.*

@Composable
fun ConsentsScreen(
    viewModel: ConsentsViewModel = viewModel()
) {
    val pendingConsents by viewModel.pendingConsents.collectAsState()
    val approvedConsents by viewModel.approvedConsents.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val context = LocalContext.current
    
    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Consents",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Text(
                    text = "Pending Requests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }
            
            items(pendingConsents) { consent ->
                ConsentCard(
                    consent = consent,
                    onApprove = {
                        Toast.makeText(context, "Consent approved", Toast.LENGTH_SHORT).show()
                    },
                    onDeny = {
                        Toast.makeText(context, "Consent denied", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Approved Consents",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }
            
            items(approvedConsents) { consent ->
                ApprovedConsentCard(consent = consent)
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ConsentCard(
    consent: Consent,
    onApprove: () -> Unit,
    onDeny: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = consent.facilityName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = consent.requestType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                
                Chip(
                    text = consent.timestamp,
                    color = TextTertiary
                )
            }
            
            if (consent.description.isNotEmpty()) {
                Text(
                    text = consent.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDeny,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Error
                    )
                ) {
                    Text("Deny")
                }
                
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Approve")
                }
            }
        }
    }
}

@Composable
fun ApprovedConsentCard(consent: Consent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardGreen)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Success,
                modifier = Modifier.size(24.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = consent.facilityName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = consent.requestType,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Text(
                text = consent.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun Chip(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}
