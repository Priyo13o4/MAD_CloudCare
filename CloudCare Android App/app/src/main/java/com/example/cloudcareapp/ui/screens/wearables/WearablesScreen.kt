package com.example.cloudcareapp.ui.screens.wearables

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cloudcareapp.data.model.*
import com.example.cloudcareapp.ui.theme.*

@Composable
fun WearablesScreen(
    viewModel: WearablesViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (val state = uiState) {
        is WearablesUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is WearablesUiState.Success -> {
            WearablesContent(
                devices = state.devices,
                healthSummary = state.healthSummary,
                insights = state.insights,
                onBackClick = onBackClick
            )
        }
        is WearablesUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${state.message}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearablesContent(
    devices: List<WearableDevice>,
    healthSummary: HealthSummary,
    insights: List<HealthInsight>,
    onBackClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wearables & Devices") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(paddingValues),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Calories Goal Card
        item {
            CaloriesGoalCard(
                current = healthSummary.calories,
                goal = healthSummary.caloriesGoal,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        // Connected Devices Section
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Connected Devices",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { /* Add device */ }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Device",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Device")
                    }
                }
            }
        }
        
        items(devices) { device ->
            DeviceCard(
                device = device,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        // Tab Selector
        item {
            TabSelector(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        // Today's Health Summary Card
        item {
            HealthSummaryCard(
                healthSummary = healthSummary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        // Health Insights Section
        item {
            Text(
                text = "Health Insights",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        items(insights) { insight ->
            InsightCard(
                insight = insight,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        // Heart Rate Chart
        item {
            HeartRateChartCard(
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    }
}

@Composable
fun CaloriesGoalCard(
    current: Int,
    goal: Int,
    modifier: Modifier = Modifier
) {
    val percentage = (current.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Calories Goal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Circular Progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                CircularProgressIndicator(
                    current = current,
                    goal = goal,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun CircularProgressIndicator(
    current: Int,
    goal: Int,
    modifier: Modifier = Modifier
) {
    val percentage = (current.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    val animatedPercentage = remember { Animatable(0f) }
    
    LaunchedEffect(percentage) {
        animatedPercentage.animateTo(
            targetValue = percentage,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 24.dp.toPx()
            
            // Background circle
            drawArc(
                color = Color(0xFFE5E7EB),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        CaloriesColor,
                        CaloriesColor.copy(alpha = 0.7f)
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360f * animatedPercentage.value,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = current.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "of $goal calories",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun DeviceCard(
    device: WearableDevice,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Device Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardOrange),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = CaloriesColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = device.type,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "${device.dataPointsSynced} data points synced",
                    style = MaterialTheme.typography.bodySmall,
                    color = Success
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Wifi,
                        contentDescription = null,
                        tint = ConnectedGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Connected",
                        style = MaterialTheme.typography.bodySmall,
                        color = ConnectedGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.BatteryChargingFull,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${device.batteryLevel}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
        
        Divider(modifier = Modifier.padding(horizontal = 16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Last sync: ${device.lastSyncTime}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun TabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TabButton(
            text = "Today's View",
            icon = Icons.Filled.Visibility,
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            text = "Weekly Trends",
            icon = Icons.Filled.BarChart,
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabButton(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Primary else SurfaceVariant
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Color.White else TextSecondary
            )
        }
    }
}

@Composable
fun HealthSummaryCard(
    healthSummary: HealthSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Today's Health Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // 2x2 Grid of metrics
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HealthMetricMiniCard(
                            title = "Steps Today",
                            value = healthSummary.steps.toString(),
                            change = "+${healthSummary.stepsChange}%",
                            icon = Icons.Filled.DirectionsWalk,
                            modifier = Modifier.weight(1f)
                        )
                        HealthMetricMiniCard(
                            title = "Heart Rate",
                            value = "${healthSummary.heartRate} bpm",
                            change = healthSummary.heartRateStatus,
                            icon = Icons.Filled.Favorite,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HealthMetricMiniCard(
                            title = "Sleep",
                            value = "${healthSummary.sleepHours.toInt()}h",
                            change = "+${healthSummary.sleepChange}%",
                            icon = Icons.Filled.Nightlight,
                            modifier = Modifier.weight(1f)
                        )
                        HealthMetricMiniCard(
                            title = "Calories",
                            value = "${healthSummary.calories} cal",
                            change = "${healthSummary.caloriesPercentage}%",
                            icon = Icons.Filled.LocalFireDepartment,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HealthMetricMiniCard(
    title: String,
    value: String,
    change: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = change,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun InsightCard(
    insight: HealthInsight,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, iconColor) = when (insight.type) {
        InsightType.STEPS -> CardBlue to StepsColor
        InsightType.HEART_RATE -> CardPink to HeartRateColor
        InsightType.SLEEP -> CardPurple to SleepColor
        InsightType.CALORIES -> CardOrange to CaloriesColor
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (insight.type) {
                        InsightType.STEPS -> Icons.Filled.TrendingUp
                        InsightType.HEART_RATE -> Icons.Filled.Favorite
                        InsightType.SLEEP -> Icons.Filled.Nightlight
                        InsightType.CALORIES -> Icons.Filled.LocalFireDepartment
                    },
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = insight.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun HeartRateChartCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Heart Rate Today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            // Simple chart placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Background),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val dataPoint = 80f // Heart rate value
                    val maxValue = 100f
                    val yPosition = size.height - (dataPoint / maxValue * size.height)
                    val xPosition = size.width * 0.5f
                    
                    drawCircle(
                        color = HeartRateColor,
                        radius = 8.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(xPosition, yPosition)
                    )
                }
            }
        }
    }
}
