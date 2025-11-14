package com.example.cloudcareapp.ui.screens.records

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen() {
    var showError by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Health Records",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Last updated: 8:26:55 PM",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { showError = !showError }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                }
            }
        }
        
        // Error Banner
        if (showError) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ErrorLight.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Unable to load records",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Error
                        )
                        Text(
                            text = "HTTP 500: Internal Server Error",
                            style = MaterialTheme.typography.bodySmall,
                            color = Error.copy(alpha = 0.8f)
                        )
                    }
                    TextButton(onClick = { showError = false }) {
                        Text("Try Again")
                    }
                }
            }
        }
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Surface,
            contentColor = Primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("All (0)") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Lab Reports (0)") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Prescriptions (0)") }
            )
        }
        
        // Empty State
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = TextTertiary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Unable to load records",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please check your connection and try again.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showError = !showError },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Retry Loading")
                }
            }
        }
    }
}
