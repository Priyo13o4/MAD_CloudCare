package com.example.cloudcareapp.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cloudcareapp.data.model.*
import com.example.cloudcareapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToWearables: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToFacilities: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToConsents: () -> Unit = {},
    onNavigateToDevices: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    
    // Format last sync time for display
    val formattedLastSync = com.example.cloudcareapp.data.cache.AppDataCache.getFormattedLastSyncTime()
    
    when (val state = uiState) {
        is DashboardUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is DashboardUiState.Success -> {
            DashboardContent(
                patient = state.patient,
                stats = state.stats,
                activities = state.recentActivities,
                healthSummary = state.healthSummary,
                isUsingRealData = state.isUsingRealData,
                errorMessage = state.errorMessage,
                onNavigateToWearables = onNavigateToWearables,
                onNavigateToRecords = onNavigateToRecords,
                onNavigateToFacilities = onNavigateToFacilities,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToConsents = onNavigateToConsents,
                onNavigateToDevices = onNavigateToDevices,
                onRefresh = { viewModel.refresh() },
                lastSyncTime = formattedLastSync,
                isSyncing = isSyncing
            )
        }
        is DashboardUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${state.message}")
            }
        }
    }
}

@Composable
fun DashboardContent(
    patient: Patient,
    stats: DashboardStats,
    activities: List<Activity>,
    healthSummary: HealthSummary?,
    isUsingRealData: Boolean,
    errorMessage: String?,
    onNavigateToWearables: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToFacilities: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToConsents: () -> Unit = {},
    onNavigateToDevices: () -> Unit = onNavigateToWearables,
    onRefresh: () -> Unit = {},
    lastSyncTime: String = "Never",
    isSyncing: Boolean = false
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        item {
            WelcomeCard(patientName = patient.name)
        }
        
        // Health Summary Card (Real Data from Backend)
        if (healthSummary != null) {
            item {
                HealthSummaryCard(
                    healthSummary = healthSummary,
                    isUsingRealData = isUsingRealData,
                    errorMessage = errorMessage,
                    onNavigateToWearables = onNavigateToWearables,
                    onRefresh = onRefresh,
                    lastSyncTime = lastSyncTime,
                    isSyncing = isSyncing
                )
            }
        }
        
        // Stats Grid
        item {
            StatsGrid(
                stats = stats,
                onFacilitiesClick = onNavigateToFacilities,
                onRecordsClick = onNavigateToRecords,
                onConsentsClick = onNavigateToConsents,
                onDevicesClick = onNavigateToDevices
            )
        }
        
        // Recent Activity
        item {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        
        items(activities) { activity ->
            ActivityItem(activity = activity)
        }
        
        // Quick Actions
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        
        item {
            QuickActionsGrid(
                onLinkFacilityClick = onNavigateToFacilities,
                onViewRecordsClick = onNavigateToRecords,
                onConnectDeviceClick = onNavigateToWearables,
                onProfileClick = onNavigateToProfile
            )
        }
        
        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun WelcomeCard(patientName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Welcome back!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = patientName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.WatchLater,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Your health information is secure and up to date.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Stay healthy and active every day",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StatsGrid(
    stats: DashboardStats,
    onFacilitiesClick: () -> Unit,
    onRecordsClick: () -> Unit,
    onConsentsClick: () -> Unit = {},
    onDevicesClick: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Linked\nFacilities",
                value = stats.linkedFacilities.toString(),
                icon = Icons.Filled.LocalHospital,
                backgroundColor = CardBlue,
                iconColor = Primary,
                onClick = onFacilitiesClick
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Health\nRecords",
                value = stats.healthRecords.toString(),
                icon = Icons.Filled.Description,
                backgroundColor = CardGreen,
                iconColor = Success,
                onClick = onRecordsClick
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Pending\nConsents",
                value = stats.pendingConsents.toString(),
                icon = Icons.Filled.Shield,
                backgroundColor = CardOrange,
                iconColor = Warning,
                onClick = onConsentsClick,
                showArrow = true
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Connected\nDevices",
                value = stats.connectedDevices.toString(),
                icon = Icons.Filled.Watch,
                backgroundColor = CardPurple,
                iconColor = Secondary,
                onClick = onDevicesClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    showArrow: Boolean = false
) {
    Card(
        modifier = modifier.height(155.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                        maxLines = 2,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Arrow icon in top-right corner if showArrow is true
            if (showArrow) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "Navigate",
                    tint = iconColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ActivityItem(activity: Activity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.5.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Primary)
                    .align(Alignment.CenterVertically)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = activity.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    onLinkFacilityClick: () -> Unit,
    onViewRecordsClick: () -> Unit,
    onConnectDeviceClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                title = "Link Facility",
                subtitle = "Connect with healthcare providers",
                icon = Icons.Outlined.Add,
                onClick = onLinkFacilityClick
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                title = "View Records",
                subtitle = "Access your health records",
                icon = Icons.Outlined.Description,
                onClick = onViewRecordsClick
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                title = "Connect Device",
                subtitle = "Sync wearable devices",
                icon = Icons.Outlined.Watch,
                onClick = onConnectDeviceClick
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                title = "Privacy Settings",
                subtitle = "Manage data sharing",
                icon = Icons.Outlined.Shield,
                onClick = onProfileClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.5.dp
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }
        }
    }
}

/**
 * Health Summary Card - Displays real-time health metrics from backend
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthSummaryCard(
    healthSummary: HealthSummary,
    isUsingRealData: Boolean,
    errorMessage: String?,
    onNavigateToWearables: () -> Unit,
    onRefresh: () -> Unit = {},
    lastSyncTime: String = "Never",
    isSyncing: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        onClick = onNavigateToWearables
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with sync button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Health Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                // Sync Button
                IconButton(
                    onClick = { onRefresh() },
                    enabled = !isSyncing,
                    modifier = Modifier.size(40.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Sync Data",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Last sync time
            if (lastSyncTime.isNotEmpty() && lastSyncTime != "Never") {
                Text(
                    text = "Last synced: $lastSyncTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Steps
                HealthMetricItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.DirectionsWalk,
                    label = "Steps",
                    value = healthSummary.steps.toString(),
                    change = healthSummary.stepsChange,
                    iconTint = Primary
                )
                
                // Heart Rate
                HealthMetricItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.FavoriteBorder,
                    label = "Heart Rate",
                    value = "${healthSummary.heartRate} bpm",
                    change = null,
                    subtitle = healthSummary.heartRateStatus,
                    iconTint = Color(0xFFE91E63)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Calories
                HealthMetricItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.LocalFireDepartment,
                    label = "Calories",
                    value = healthSummary.calories.toString(),
                    change = null,
                    subtitle = "${healthSummary.caloriesPercentage}% of goal",
                    iconTint = Color(0xFFFF9800)
                )
                
                // Sleep
                HealthMetricItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Bedtime,
                    label = "Sleep",
                    value = if (healthSummary.sleepHours > 0) "${String.format("%.1f", healthSummary.sleepHours)}h" else "No data",
                    change = if (healthSummary.sleepHours > 0) healthSummary.sleepChange else null,
                    iconTint = Color(0xFF9C27B0)
                )
            }
            
            // Error message if using fallback data
            if (!isUsingRealData && errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF3E0)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HealthMetricItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    change: Int?,
    subtitle: String? = null,
    iconTint: Color = Primary
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            
            // Show change percentage or subtitle
            if (change != null && change != 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (change > 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (change > 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${if (change > 0) "+" else ""}$change%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (change > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            } else if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}
