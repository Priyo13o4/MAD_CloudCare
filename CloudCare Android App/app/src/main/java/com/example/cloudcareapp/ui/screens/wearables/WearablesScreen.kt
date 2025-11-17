@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cloudcareapp.ui.screens.wearables

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cloudcareapp.data.model.*
import com.example.cloudcareapp.ui.components.QRScannerScreen
import com.example.cloudcareapp.ui.screens.wearables.cards.SleepDetailCard
import com.example.cloudcareapp.ui.screens.wearables.cards.StepsDetailCard
import com.example.cloudcareapp.ui.screens.wearables.cards.HeartRateDetailCard
import com.example.cloudcareapp.ui.screens.wearables.cards.CaloriesDetailCard
import com.example.cloudcareapp.ui.screens.wearables.cards.DistanceDetailCard
import com.example.cloudcareapp.ui.theme.*
import com.google.gson.Gson
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearablesScreen(
    navController: NavController,
    wearablesViewModel: WearablesViewModel = viewModel()
) {
    val uiState by wearablesViewModel.uiState.collectAsState()
    val weeklyData by wearablesViewModel.weeklyData.collectAsState()
    val isSyncing by wearablesViewModel.isSyncing.collectAsState()
    val selectedRange by wearablesViewModel.selectedDateRange.collectAsState()
    
    // Format last sync time for display
    val formattedLastSync = com.example.cloudcareapp.data.cache.AppDataCache.getFormattedLastSyncTime()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Wearables & Devices",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        when (uiState) {
            is WearablesUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is WearablesUiState.Success -> {
                val successState = uiState as WearablesUiState.Success
                WearablesContent(
                    devices = successState.devices,
                    healthSummary = successState.healthSummary,
                    insights = successState.insights,
                    weeklyData = weeklyData,
                    onRefresh = { wearablesViewModel.refresh() },
                    wearablesViewModel = wearablesViewModel,
                    lastSyncTime = formattedLastSync,
                    isSyncing = isSyncing,
                    selectedRange = selectedRange,
                    modifier = Modifier.padding(padding)
                )
            }
            
            is WearablesUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = (uiState as WearablesUiState.Error).message,
                            color = Color.Red
                        )
                        Button(onClick = { wearablesViewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
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
    weeklyData: WeeklyDataState,
    onRefresh: () -> Unit,
    wearablesViewModel: WearablesViewModel,
    lastSyncTime: String = "Never",
    isSyncing: Boolean = false,
    selectedRange: Int,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showPairingDialog by remember { mutableStateOf(false) }
    
    val aggregatedData = (weeklyData as? WeeklyDataState.Success)?.data
    val stepsMetrics = aggregatedData?.metrics?.get("steps")
    val heartMetrics = aggregatedData?.metrics?.get("heart_rate")
    val caloriesMetrics = aggregatedData?.metrics?.get("calories")
    val distanceMetrics = aggregatedData?.metrics?.get("distance")
    val sleepMetrics = aggregatedData?.metrics?.get("sleep")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Calories Goal Card
        item {
            CaloriesGoalCard(
                summary = healthSummary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        // Connected Devices Section
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Connected Devices",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (lastSyncTime.isNotEmpty()) {
                            Text(
                                text = "Last synced: $lastSyncTime",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    
                    // Sync Button
                    IconButton(
                        onClick = onRefresh,
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
                                contentDescription = "Sync Devices",
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    // Add Device Button
                    TextButton(onClick = { showPairingDialog = true }) {
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
        
        // Content based on selected tab
        if (selectedTab == 0) {
            // Today's View
            item {
                HealthSummaryCard(
                    healthSummary = healthSummary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Sleep Detail Card
            item {
                SleepDetailCard(
                    sleepMetrics = sleepMetrics,
                    todaySleepHours = healthSummary.sleepHours,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Steps Detail Card
            item {
                StepsDetailCard(
                    stepMetrics = stepsMetrics,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Heart Rate Detail Card
            item {
                HeartRateDetailCard(
                    heartRateMetrics = heartMetrics,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Calories Detail Card
            item {
                CaloriesDetailCard(
                    calorieMetrics = caloriesMetrics,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Distance Detail Card
            item {
                DistanceDetailCard(
                    distanceMetrics = distanceMetrics,
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
        } else {
            // Weekly Trends
            item {
                when (weeklyData) {
                    is WeeklyDataState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is WeeklyDataState.Success -> {
                        WeeklyTrendsSection(
                            data = weeklyData.data,
                            selectedRange = selectedRange,
                            onRangeSelected = { wearablesViewModel.setDateRange(it) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    is WeeklyDataState.Error -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Failed to load weekly data",
                                    color = Color.Red
                                )
                                Button(onClick = onRefresh) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Full-screen QR Scanner
    if (showPairingDialog) {
        QRScannerOverlay(
            wearablesViewModel = wearablesViewModel,
            onDismiss = { showPairingDialog = false }
        )
    }
}

@Composable
fun CaloriesGoalCard(
    summary: HealthSummary,
    modifier: Modifier = Modifier
) {
    MultiMetricRingsCard(summary = summary, modifier = modifier)
}

@Composable
fun QRScannerOverlay(
    wearablesViewModel: WearablesViewModel,
    onDismiss: () -> Unit
) {
    var pairingStatus by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Box(modifier = Modifier.fillMaxSize()) {
        QRScannerScreen(
            onQRCodeScanned = { qrCode ->
                if (!isProcessing) {
                    isProcessing = true
                    pairingStatus = null
                    
                    // Parse QR code outside of lambda
                    val pairingData = try {
                        Gson().fromJson(qrCode, PairingData::class.java)
                    } catch (e: Exception) {
                        pairingStatus = "Invalid QR code format"
                        isProcessing = false
                        return@QRScannerScreen
                    }
                    
                    // Send pairing request to backend
                    wearablesViewModel.pairDevice(pairingData) { success, message ->
                        pairingStatus = message
                        if (success) {
                            // Close scanner after 2 seconds on success
                            scope.launch {
                                delay(2000)
                                onDismiss()
                                isProcessing = false
                            }
                        } else {
                            isProcessing = false
                        }
                    }
                }
            },
            onDismiss = { 
                onDismiss()
                isProcessing = false
                pairingStatus = null
            }
        )
        
        // Status overlay
        pairingStatus?.let { status ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (status.contains("already", ignoreCase = true) || status.contains("success", ignoreCase = true))
                        Success.copy(alpha = 0.9f)
                    else
                        Error.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun MultiMetricRingsCard(
    summary: HealthSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Today's Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Concentric rings showing calories, steps, and sleep
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                ConcentricRings(
                    caloriesProgress = summary.calories.toFloat() / summary.caloriesGoal.toFloat().coerceAtLeast(1f),
                    stepsProgress = summary.steps.toFloat() / 10000f,
                    sleepProgress = summary.sleepHours.toFloat() / 8f,
                    modifier = Modifier.fillMaxSize()
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${summary.calories}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = CaloriesColor
                    )
                    Text(
                        text = "calories",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricLegendItem(
                    color = CaloriesColor,
                    label = "Calories",
                    value = "${summary.calories}/${summary.caloriesGoal}"
                )
                MetricLegendItem(
                    color = StepsColor,
                    label = "Steps",
                    value = "${summary.steps}/10k"
                )
                MetricLegendItem(
                    color = SleepColor,
                    label = "Sleep",
                    value = String.format("%.1fh/8h", summary.sleepHours)
                )
            }
        }
    }
}

@Composable
fun ConcentricRings(
    caloriesProgress: Float,
    stepsProgress: Float,
    sleepProgress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 18.dp.toPx()
        val spacing = 8.dp.toPx()
        
        // Outer ring - Calories (largest)
        val caloriesRadius = (size.minDimension / 2) - strokeWidth / 2
        drawArc(
            color = CaloriesColor.copy(alpha = 0.15f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(
                (size.width - caloriesRadius * 2) / 2,
                (size.height - caloriesRadius * 2) / 2
            ),
            size = Size(caloriesRadius * 2, caloriesRadius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(CaloriesColor, CaloriesColor.copy(alpha = 0.7f))
            ),
            startAngle = -90f,
            sweepAngle = 360f * caloriesProgress,
            useCenter = false,
            topLeft = Offset(
                (size.width - caloriesRadius * 2) / 2,
                (size.height - caloriesRadius * 2) / 2
            ),
            size = Size(caloriesRadius * 2, caloriesRadius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Middle ring - Steps
        val stepsRadius = caloriesRadius - strokeWidth - spacing
        drawArc(
            color = StepsColor.copy(alpha = 0.15f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(
                (size.width - stepsRadius * 2) / 2,
                (size.height - stepsRadius * 2) / 2
            ),
            size = Size(stepsRadius * 2, stepsRadius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(StepsColor, StepsColor.copy(alpha = 0.7f))
            ),
            startAngle = -90f,
            sweepAngle = 360f * stepsProgress,
            useCenter = false,
            topLeft = Offset(
                (size.width - stepsRadius * 2) / 2,
                (size.height - stepsRadius * 2) / 2
            ),
            size = Size(stepsRadius * 2, stepsRadius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Inner ring - Sleep
        val sleepRadius = stepsRadius - strokeWidth - spacing
        drawArc(
            color = SleepColor.copy(alpha = 0.15f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(
                (size.width - sleepRadius * 2) / 2,
                (size.height - sleepRadius * 2) / 2
            ),
            size = Size(sleepRadius * 2, sleepRadius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(SleepColor, SleepColor.copy(alpha = 0.7f))
            ),
            startAngle = -90f,
            sweepAngle = 360f * sleepProgress,
            useCenter = false,
            topLeft = Offset(
                (size.width - sleepRadius * 2) / 2,
                (size.height - sleepRadius * 2) / 2
            ),
            size = Size(sleepRadius * 2, sleepRadius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun MetricLegendItem(
    color: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
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
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device Icon with gradient background
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                CaloriesColor.copy(alpha = 0.2f),
                                HeartRateColor.copy(alpha = 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Watch,
                    contentDescription = null,
                    tint = CaloriesColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    device.name?.let { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    } ?: run {
                        Text(
                            text = "Unknown Device",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                    // Connected status badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ConnectedGreen.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(ConnectedGreen)
                            )
                            Text(
                                text = "Connected",
                                style = MaterialTheme.typography.labelSmall,
                                color = ConnectedGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                device.type?.let { type ->
                    Text(
                        text = type,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } ?: run {
                    Text(
                        text = "Unknown Type",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Last sync info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Sync,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Last sync: ${device.last_sync_time ?: "Never"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary.copy(alpha = 0.8f)
                    )
                }
            }
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
            text = "Health Trends",
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
                        InsightType.STEPS -> Icons.AutoMirrored.Filled.TrendingUp
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

@Composable
fun WeeklyTrendsSection(
    data: AggregatedMetricsResponse,
    selectedRange: Int,
    onRangeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Health Trends",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // Date range selector
            DateRangeSelector(
                selectedDays = selectedRange,
                onRangeSelected = onRangeSelected
            )
        }
        
        // Steps Chart
        data.metrics["steps"]?.let { stepsData ->
            if (stepsData.isNotEmpty()) {
                WeeklyMetricCard(
                    title = "Steps",
                    dataPoints = stepsData,
                    color = StepsColor,
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    unit = "steps"
                )
            }
        }
        
        // Heart Rate Chart
        data.metrics["heart_rate"]?.let { heartRateData ->
            if (heartRateData.isNotEmpty()) {
                WeeklyMetricCard(
                    title = "Heart Rate",
                    dataPoints = heartRateData,
                    color = HeartRateColor,
                    icon = Icons.Filled.Favorite,
                    unit = "bpm",
                    showAverage = true
                )
            }
        }
        
        // Calories Chart
        data.metrics["calories"]?.let { caloriesData ->
            if (caloriesData.isNotEmpty()) {
                WeeklyMetricCard(
                    title = "Calories",
                    dataPoints = caloriesData,
                    color = CaloriesColor,
                    icon = Icons.Filled.LocalFireDepartment,
                    unit = "kcal"
                )
            }
        }
        
        // Sleep Chart
        data.metrics["sleep"]?.let { sleepData ->
            if (sleepData.isNotEmpty()) {
                WeeklyMetricCard(
                    title = "Sleep",
                    dataPoints = sleepData,
                    color = SleepColor,
                    icon = Icons.Filled.Nightlight,
                    unit = "hours"
                )
            }
        }
        
        // Distance Chart
        data.metrics["distance"]?.let { distanceData ->
            if (distanceData.isNotEmpty()) {
                WeeklyMetricCard(
                    title = "Distance",
                    dataPoints = distanceData,
                    color = StepsColor,
                    icon = Icons.Filled.Route,
                    unit = "km"
                )
            }
        }
    }
}

@Composable
fun DateRangeSelector(
    selectedDays: Int,
    onRangeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val ranges = listOf(1 to "1D", 7 to "7D", 30 to "30D", 365 to "1Y")
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ranges.forEach { (days, label) ->
            Button(
                onClick = { onRangeSelected(days) },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedDays == days) Primary else SurfaceVariant,
                    contentColor = if (selectedDays == days) Color.White else TextSecondary
                )
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selectedDays == days) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun WeeklyMetricCard(
    title: String,
    dataPoints: List<AggregatedDataPoint>,
    color: Color,
    icon: ImageVector,
    unit: String,
    showAverage: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Fix units
    val displayUnit = when {
        unit == "km" && title == "Distance" -> "km"
        unit == "cal" -> "kcal"
        else -> unit
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        color.copy(alpha = 0.2f),
                                        color.copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Show stats
                Column(horizontalAlignment = Alignment.End) {
                    val displayValue = if (showAverage) {
                        dataPoints.map { it.avg }.average()
                    } else {
                        dataPoints.map { it.total }.sum()
                    }
                    
                    // Convert distance to km
                    val finalValue = if (title == "Distance") {
                        displayValue / 1000.0
                    } else {
                        displayValue
                    }
                    
                    Text(
                        text = if (title == "Distance") {
                            String.format("%.2f %s", finalValue, displayUnit)
                        } else {
                            String.format("%.0f %s", finalValue, displayUnit)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = if (showAverage) "Avg" else "Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            
            // Professional Vico Chart
            if (dataPoints.isNotEmpty()) {
                val modelProducer = remember { CartesianChartModelProducer.build() }
                
                LaunchedEffect(dataPoints) {
                    val values = if (showAverage) {
                        dataPoints.map {
                            if (title == "Distance") it.avg / 1000.0 else it.avg
                        }
                    } else {
                        dataPoints.map {
                            if (title == "Distance") it.total / 1000.0 else it.total
                        }
                    }
                    modelProducer.tryRunTransaction {
                        lineSeries { series(values) }
                    }
                }
                
                // Apple Health style charts based on metric type
                when (title) {
                    "Steps", "Calories", "Distance" -> {
                        // Use bar chart for steps, calories, distance
                        AppleHealthBarChart(
                            data = dataPoints.map { if (showAverage) it.avg else it.total },
                            barColor = color,
                            chartKey = "trends_${title}_${dataPoints.size}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                    "Heart Rate" -> {
                        // Use centered bar chart with min/max for heart rate
                        val rangeData = dataPoints.map { (it.min) to (it.max) }
                        val avgValue = dataPoints.map { it.avg }.average()
                        AppleHealthHeartRateChart(
                            data = rangeData,
                            highlightValue = avgValue,
                            chartKey = "trends_${title}_${dataPoints.size}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                    "Sleep" -> {
                        // Use sleep-specific chart for sleep hours
                        val rangeData = dataPoints.map { (it.min) to (it.max) }
                        val avgValue = dataPoints.map { it.avg }.average()
                        AppleHealthSleepChart(
                            data = rangeData,
                            highlightValue = avgValue,
                            chartKey = "trends_${title}_${dataPoints.size}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                    else -> {
                        // Use line chart for other metrics
                        CustomSmoothLineChart(
                            data = if (showAverage) dataPoints.map { if (title == "Distance") it.avg / 1000.0 else it.avg }
                            else dataPoints.map { if (title == "Distance") it.total / 1000.0 else it.total },
                            lineColor = color,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }
                
                // Date labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dataPoints.take(if (dataPoints.size > 7) 4 else dataPoints.size).forEachIndexed { index, point ->
                        val dayLabel = try {
                            val parts = point.date.split("-")
                            if (parts.size == 3) {
                                val month = parts[1].toInt()
                                val day = parts[2].toInt()
                                "${month}/${day}"
                            } else {
                                point.date.takeLast(5)
                            }
                        } catch (e: Exception) {
                            point.date.takeLast(5)
                        }
                        
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// Removed SimpleLineChart - replaced with CustomSmoothLineChart

@Composable
fun CustomSmoothLineChart(
    data: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    Canvas(modifier = modifier.padding(vertical = 8.dp)) {
        val width = size.width
        val height = size.height
        val paddingX = 8.dp.toPx()
        val paddingY = 8.dp.toPx()

        val points = data.mapIndexed { index, value ->
            val x = paddingX + (width - 2 * paddingX) * (index.toFloat() / (data.size - 1).coerceAtLeast(1))
            val max = data.maxOrNull() ?: 1.0
            val min = data.minOrNull() ?: 0.0
            val range = (max - min).coerceAtLeast(1.0)
            val y = paddingY + (height - 2 * paddingY) - ((value - min) / range * (height - 2 * paddingY)).toFloat()
            Offset(x, y)
        }

        // Draw grid lines
        val gridPaintColor = Color.LightGray.copy(alpha = 0.15f)
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = paddingY + i * (height - 2 * paddingY) / gridLines
            drawLine(color = gridPaintColor, start = Offset(paddingX, y), end = Offset(width - paddingX, y), strokeWidth = 1.dp.toPx())
        }

        // Build smooth path using quadratic Bezier midpoints
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val midX = (p0.x + p1.x) / 2f
                val midY = (p0.y + p1.y) / 2f
                quadraticBezierTo(p0.x, p0.y, midX, midY)
            }
            // Line to last point
            val last = points.last()
            lineTo(last.x, last.y)
        }

        // Area fill under the curve
        val areaPath = androidx.compose.ui.graphics.Path().apply {
            addPath(path)
            // close down to baseline
            val lastX = points.last().x
            val baseline = height - paddingY
            lineTo(lastX, baseline)
            lineTo(points.first().x, baseline)
            close()
        }
        drawPath(path = areaPath, color = lineColor.copy(alpha = 0.12f))

        // Draw the line
        drawPath(path = path, color = lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

        // Draw points
        points.forEach { pt ->
            drawCircle(color = lineColor.copy(alpha = 0.3f), radius = 6.dp.toPx(), center = pt)
            drawCircle(color = lineColor, radius = 3.dp.toPx(), center = pt)
        }
    }
}

/**
 * Custom Bar Chart matching Apple Health design
 * Features: Vertical bars with rounded tops, gradient fills, smooth animations
 */
@Composable
fun CustomBarChart(
    data: List<Double>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val maxValue = data.maxOrNull() ?: 1.0
    val animatedValues = data.map { value ->
        val animatedValue = remember { Animatable(0f) }
        LaunchedEffect(value) {
            animatedValue.animateTo(
                targetValue = (value / maxValue).toFloat(),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        animatedValue.value
    }
    
    Canvas(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        val barCount = data.size
        val spacing = 8.dp.toPx()
        val totalSpacing = spacing * (barCount - 1)
        val barWidth = (size.width - totalSpacing) / barCount
        val maxHeight = size.height - 40.dp.toPx() // Reserve space for axis
        
        // Draw horizontal grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = maxHeight * (i.toFloat() / gridLines)
            drawLine(
                color = Color.Gray.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // Draw bars
        animatedValues.forEachIndexed { index, animatedHeight ->
            val x = index * (barWidth + spacing)
            val barHeight = maxHeight * animatedHeight
            val y = maxHeight - barHeight
            
            // Create rounded rect path for bar
            val barPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(
                            left = x,
                            top = y,
                            right = x + barWidth,
                            bottom = maxHeight
                        ),
                        topLeft = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                        topRight = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                )
            }
            
            // Draw bar with gradient
            val gradient = Brush.verticalGradient(
                colors = listOf(
                    barColor,
                    barColor.copy(alpha = 0.7f)
                ),
                startY = y,
                endY = maxHeight
            )
            
            drawPath(
                path = barPath,
                brush = gradient
            )
        }
    }
}

@Composable
fun CardEmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = TextSecondary.copy(alpha = 0.7f)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

/**
 * Simple stat item for metrics
 */
@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/**
 * Apple Health style bar chart - clean vertical bars with rounded tops
 * Now with interactive tap to show values
 */
@Composable
fun AppleHealthBarChart(
    data: List<Double>,
    barColor: Color,
    modifier: Modifier = Modifier,
    chartKey: String = "bar_chart_${data.hashCode()}"
) {
    if (data.isEmpty()) return
    
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxValue = data.maxOrNull() ?: 1.0
    
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val barCount = data.size
                        val spacing = 12.dp.toPx()
                        val totalSpacing = spacing * (barCount - 1)
                        val barWidth = (size.width - totalSpacing) / barCount
                        
                        val tappedIndex = ((offset.x) / (barWidth + spacing)).toInt()
                        selectedIndex = if (tappedIndex in data.indices) tappedIndex else null
                    }
                }
        ) {
            val barCount = data.size
            val spacing = 12.dp.toPx()
            val totalSpacing = spacing * (barCount - 1)
            val barWidth = (size.width - totalSpacing) / barCount
            val maxHeight = size.height
            
            // Draw bars with Apple Health style
            data.forEachIndexed { index, value ->
                val normalizedHeight = (value / maxValue).toFloat()
                val x = index * (barWidth + spacing)
                val barHeight = maxHeight * normalizedHeight
                val y = maxHeight - barHeight
                
                // Rounded top rectangle
                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(
                                left = x,
                                top = y,
                                right = x + barWidth,
                                bottom = maxHeight
                            ),
                            topLeft = CornerRadius(barWidth / 2, barWidth / 2),
                            topRight = CornerRadius(barWidth / 2, barWidth / 2)
                        )
                    )
                }
                
                // Highlight selected bar
                val isSelected = selectedIndex == index
                drawPath(
                    path = path,
                    color = if (isSelected) barColor else barColor.copy(alpha = 0.8f)
                )
            }
        }
        
        // Show value overlay when bar is selected
        selectedIndex?.let { index ->
            if (index in data.indices) {
                val value = data[index].toInt()
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = barColor,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "$value",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Apple Health style sleep chart - centered bar chart showing sleep hours with min/max range
 */
@Composable
fun AppleHealthSleepChart(
    data: List<Pair<Double, Double>>, // List of (min, max) pairs in hours
    highlightValue: Double,
    modifier: Modifier = Modifier,
    chartKey: String = "sleep_chart_${data.hashCode()}"
) {
    if (data.isEmpty()) return
    
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    // Calculate averages - this is the center line
    val averages = data.map { (min, max) -> (min + max) / 2.0 }
    val globalAvg = averages.average()
    val globalMin = data.minOf { it.first }
    val globalMax = data.maxOf { it.second }
    val range = (globalMax - globalMin).coerceAtLeast(1.0)
    
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val barCount = data.size
                        val spacing = 16.dp.toPx()
                        val totalSpacing = spacing * (barCount - 1)
                        val barWidth = ((size.width - totalSpacing) / barCount).coerceAtMost(40.dp.toPx())
                        val actualTotalWidth = barWidth * barCount + totalSpacing
                        val startX = (size.width - actualTotalWidth) / 2
                        
                        val tappedIndex = ((offset.x - startX) / (barWidth + spacing)).toInt()
                        selectedIndex = if (tappedIndex in data.indices) tappedIndex else null
                    }
                }
        ) {
            val barCount = data.size
            val spacing = 16.dp.toPx()
            val totalSpacing = spacing * (barCount - 1)
            val barWidth = ((size.width - totalSpacing) / barCount).coerceAtMost(40.dp.toPx())
            val actualTotalWidth = barWidth * barCount + totalSpacing
            val startX = (size.width - actualTotalWidth) / 2
            val maxHeight = size.height
            val centerY = maxHeight / 2
            
            // Draw center line (average)
            drawLine(
                color = SleepColor.copy(alpha = 0.2f),
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
            )
            
            data.forEachIndexed { index, (min, max) ->
                val x = startX + index * (barWidth + spacing) + barWidth / 2
                val avg = (min + max) / 2.0
                
                // Calculate positions based on center line (no animation)
                val minY = centerY - ((min - globalAvg) / range * maxHeight * 0.45f).toFloat()
                val maxY = centerY - ((max - globalAvg) / range * maxHeight * 0.45f).toFloat()
                val avgY = centerY - ((avg - globalAvg) / range * maxHeight * 0.45f).toFloat()
                
                val isSelected = selectedIndex == index
                val barColor = if (isSelected) SleepColor else SleepColor.copy(alpha = 0.7f)
                
                // Draw vertical range bar (thicker)
                drawLine(
                    color = barColor,
                    start = Offset(x, maxY),
                    end = Offset(x, minY),
                    strokeWidth = barWidth * 0.6f,
                    cap = StrokeCap.Round
                )
                
                // Draw min dot
                drawCircle(
                    color = barColor,
                    radius = 3.dp.toPx(),
                    center = Offset(x, minY)
                )
                
                // Draw max dot
                drawCircle(
                    color = barColor,
                    radius = 3.dp.toPx(),
                    center = Offset(x, maxY)
                )
                
                // Draw average dot (center)
                drawCircle(
                    color = barColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, avgY)
                )
                
                // Highlight the latest value if it's the last bar
                if (index == data.size - 1) {
                    val highlightY = centerY - ((highlightValue - globalAvg) / range * maxHeight * 0.45f).toFloat()
                    drawCircle(
                        color = SleepColor,
                        radius = 5.dp.toPx(),
                        center = Offset(x, highlightY)
                    )
                    // Outer ring
                    drawCircle(
                        color = SleepColor,
                        radius = 6.dp.toPx(),
                        center = Offset(x, highlightY),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
        }
        
        // Show value overlay when bar is selected
        selectedIndex?.let { index ->
            if (index in data.indices) {
                val (min, max) = data[index]
                val avg = (min + max) / 2.0
                val value = String.format("%.1f", avg)
                
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = SleepColor,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "$value hrs (${String.format("%.1f", min)}-${String.format("%.1f", max)})",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Apple Health style heart rate chart - centered bar chart with average as midpoint
 */
@Composable
fun AppleHealthHeartRateChart(
    data: List<Pair<Double, Double>>, // List of (min, max) pairs
    highlightValue: Double,
    modifier: Modifier = Modifier,
    chartKey: String = "heart_chart_${data.hashCode()}"
) {
    if (data.isEmpty()) return
    
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    // Calculate averages - this is the center line
    val averages = data.map { (min, max) -> (min + max) / 2.0 }
    val globalAvg = averages.average()
    val globalMin = data.minOf { it.first }
    val globalMax = data.maxOf { it.second }
    val range = (globalMax - globalMin).coerceAtLeast(1.0)
    
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val barCount = data.size
                        val spacing = 16.dp.toPx()
                        val totalSpacing = spacing * (barCount - 1)
                        val barWidth = ((size.width - totalSpacing) / barCount).coerceAtMost(40.dp.toPx())
                        val actualTotalWidth = barWidth * barCount + totalSpacing
                        val startX = (size.width - actualTotalWidth) / 2
                        
                        val tappedIndex = ((offset.x - startX) / (barWidth + spacing)).toInt()
                        selectedIndex = if (tappedIndex in data.indices) tappedIndex else null
                    }
                }
        ) {
            val barCount = data.size
            val spacing = 16.dp.toPx()
            val totalSpacing = spacing * (barCount - 1)
            val barWidth = ((size.width - totalSpacing) / barCount).coerceAtMost(40.dp.toPx())
            val actualTotalWidth = barWidth * barCount + totalSpacing
            val startX = (size.width - actualTotalWidth) / 2
            val maxHeight = size.height
            val centerY = maxHeight / 2
            
            // Draw center line (average)
            drawLine(
                color = HeartRateColor.copy(alpha = 0.2f),
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
            )
            
            data.forEachIndexed { index, (min, max) ->
                val x = startX + index * (barWidth + spacing) + barWidth / 2
                val avg = (min + max) / 2.0
                
                // Calculate positions based on center line (no animation)
                val minY = centerY - ((min - globalAvg) / range * maxHeight * 0.45f).toFloat()
                val maxY = centerY - ((max - globalAvg) / range * maxHeight * 0.45f).toFloat()
                val avgY = centerY - ((avg - globalAvg) / range * maxHeight * 0.45f).toFloat()
                
                val isSelected = selectedIndex == index
                val barColor = if (isSelected) HeartRateColor else HeartRateColor.copy(alpha = 0.7f)
                
                // Draw vertical range bar (thicker)
                drawLine(
                    color = barColor,
                    start = Offset(x, maxY),
                    end = Offset(x, minY),
                    strokeWidth = barWidth * 0.6f,
                    cap = StrokeCap.Round
                )
                
                // Draw min dot
                drawCircle(
                    color = barColor,
                    radius = 3.dp.toPx(),
                    center = Offset(x, minY)
                )
                
                // Draw max dot
                drawCircle(
                    color = barColor,
                    radius = 3.dp.toPx(),
                    center = Offset(x, maxY)
                )
                
                // Draw average dot (center)
                drawCircle(
                    color = barColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, avgY)
                )
                
                // Highlight the latest value if it's the last bar
                if (index == data.size - 1) {
                    val highlightY = centerY - ((highlightValue - globalAvg) / range * maxHeight * 0.45f).toFloat()
                    drawCircle(
                        color = HeartRateColor,
                        radius = 5.dp.toPx(),
                        center = Offset(x, highlightY)
                    )
                    // Outer ring
                    drawCircle(
                        color = HeartRateColor,
                        radius = 6.dp.toPx(),
                        center = Offset(x, highlightY),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
        }
        
        // Show value overlay when bar is selected
        selectedIndex?.let { index ->
            if (index in data.indices) {
                val (min, max) = data[index]
                val avg = (min + max) / 2.0
                val value = avg.toInt()
                
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = HeartRateColor,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "$value BPM (${min.toInt()}-${max.toInt()})",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


