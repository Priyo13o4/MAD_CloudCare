@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cloudcareapp.ui.screens.wearables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cloudcareapp.data.model.*
import com.example.cloudcareapp.ui.components.QRScannerScreen
import com.example.cloudcareapp.ui.screens.wearables.cards.*
import com.example.cloudcareapp.ui.theme.*
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Refactored WearablesScreen - Clean & Modular (Under 400 lines)
 * 
 * Architecture:
 * - Charts: Extracted to ui/components/charts/
 * - Cards: Modular components in wearables/cards/
 * - Dark Theme: Apple Health-style (#000000 / #1C1C1E background)
 * - No hardcoded logic: All data from backend via ViewModel
 */
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

    val formattedLastSync = com.example.cloudcareapp.data.cache.AppDataCache.getFormattedLastSyncTime()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Wearables & Devices",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1C1E)
                )
            )
        }
    ) { padding ->
        when (uiState) {
            is WearablesUiState.Loading -> {
                LoadingState(modifier = Modifier.padding(padding))
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
                ErrorState(
                    message = (uiState as WearablesUiState.Error).message,
                    onRetry = { wearablesViewModel.refresh() },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Primary)
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = message, color = Error)
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun WearablesContent(
    devices: List<WearableDevice>,
    healthSummary: HealthSummary,
    insights: List<HealthInsight>,
    weeklyData: WeeklyDataState,
    onRefresh: () -> Unit,
    wearablesViewModel: WearablesViewModel,
    lastSyncTime: String,
    isSyncing: Boolean,
    selectedRange: Int,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showPairingDialog by remember { mutableStateOf(false) }
    
    // Collect sleep trends from ViewModel
    val sleepTrends by wearablesViewModel.sleepTrends.collectAsState()
    
    // Collect heart rate trends from ViewModel
    val heartRateTrends by wearablesViewModel.heartRateTrends.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Connected Devices Section
        item {
            DevicesHeader(
                onRefresh = onRefresh,
                onAddDevice = { showPairingDialog = true },
                isSyncing = isSyncing,
                lastSyncTime = lastSyncTime,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Device Cards
        items(devices.size) { index ->
            DeviceCard(
                device = devices[index],
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
                CaloriesGoalCard(
                    summary = healthSummary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                SleepDetailCard(
                    sleepMetrics = null,
                    todaySleepHours = healthSummary.sleepHours,
                    sleepTimeInBed = healthSummary.sleepTimeInBed,
                    sleepTimeAsleep = healthSummary.sleepTimeAsleep,
                    sleepStages = healthSummary.sleepStages,
                    sleepSessions = healthSummary.sleepSessions,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                DailyStepsCard(
                    steps = healthSummary.steps,
                    goal = 10000,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                DailyHeartRateCard(
                    heartRate = healthSummary.heartRate,
                    status = healthSummary.heartRateStatus,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                DailyCaloriesCard(
                    calories = healthSummary.calories,
                    goal = healthSummary.caloriesGoal,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            // Health Trends
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
                            CircularProgressIndicator(color = Primary)
                        }
                    }

                    is WeeklyDataState.Success -> {
                        val aggregatedData = weeklyData.data

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Health Trends",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            // Steps Trends
                            aggregatedData.metrics["steps"]?.let { stepsData ->
                                if (stepsData.isNotEmpty()) {
                                    TrendsStepsCard(
                                        data = stepsData,
                                        onTimeframeChange = { timeframe ->
                                            val days = when (timeframe) {
                                                "D" -> 1
                                                "W" -> 7
                                                "M" -> 30
                                                else -> 7
                                            }
                                            wearablesViewModel.setDateRange(days)
                                        }
                                    )
                                }
                            }

                            // Energy (Calories) Trends
                            aggregatedData.metrics["calories"]?.let { caloriesData ->
                                if (caloriesData.isNotEmpty()) {
                                    TrendsEnergyCard(
                                        data = caloriesData,
                                        onTimeframeChange = { timeframe ->
                                            val days = when (timeframe) {
                                                "D" -> 1
                                                "W" -> 7
                                                "M" -> 30
                                                else -> 7
                                            }
                                            wearablesViewModel.setDateRange(days)
                                        }
                                    )
                                }
                            }

                            // Distance Trends
                            aggregatedData.metrics["distance"]?.let { distanceData ->
                                if (distanceData.isNotEmpty()) {
                                    TrendsDistanceCard(
                                        data = distanceData,
                                        onTimeframeChange = { timeframe ->
                                            val days = when (timeframe) {
                                                "D" -> 1
                                                "W" -> 7
                                                "M" -> 30
                                                else -> 7
                                            }
                                            wearablesViewModel.setDateRange(days)
                                        }
                                    )
                                }
                            }

                            // Sleep Trends - Using dedicated sleep-trends endpoint
                            if (sleepTrends.isNotEmpty()) {
                                TrendsSleepCard(
                                    sleepTrends = sleepTrends,
                                    onTimeframeChange = { timeframe ->
                                        val days = when (timeframe) {
                                            "D" -> 1
                                            "W" -> 7
                                            "M" -> 30
                                            else -> 7
                                        }
                                        wearablesViewModel.setDateRange(days)
                                    }
                                )
                            }
                            
                            // Heart Rate Trends - Using dedicated heart-rate-trends endpoint
                            if (heartRateTrends.isNotEmpty()) {
                                TrendsHeartRateCard(
                                    heartRateTrends = heartRateTrends,
                                    onTimeframeChange = { timeframe ->
                                        val days = when (timeframe) {
                                            "D" -> 1
                                            "W" -> 7
                                            "M" -> 30
                                            else -> 7
                                        }
                                        wearablesViewModel.setDateRange(days)
                                    }
                                )
                            }
                        }
                    }

                    is WeeklyDataState.Error -> {
                        ErrorCard(
                            message = weeklyData.message,
                            onRetry = onRefresh,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showPairingDialog) {
        QRScannerOverlay(
            wearablesViewModel = wearablesViewModel,
            onDismiss = { showPairingDialog = false }
        )
    }
}

@Composable
private fun DevicesHeader(
    onRefresh: () -> Unit,
    onAddDevice: () -> Unit,
    isSyncing: Boolean,
    lastSyncTime: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Connected Devices",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (lastSyncTime.isNotEmpty()) {
                    Text(
                        text = "Last synced: $lastSyncTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            IconButton(onClick = onRefresh, enabled = !isSyncing) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Sync",
                        tint = Primary
                    )
                }
            }

            TextButton(onClick = onAddDevice) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Device")
            }
        }
    }
}

@Composable
private fun DeviceCard(device: WearableDevice, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Primary.copy(alpha = 0.2f),
                                Primary.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Watch,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name ?: "Unknown Device",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = device.type ?: "Unknown Type",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = "Last sync: ${device.last_sync_time ?: "Never"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun TabSelector(
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
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            text = "Health Trends",
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Primary else Color(0xFF2C2C2E)
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Color.White else Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = message, color = Error)
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun QRScannerOverlay(
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
                    val pairingData = try {
                        Gson().fromJson(qrCode, PairingData::class.java)
                    } catch (e: Exception) {
                        pairingStatus = "Invalid QR code"
                        isProcessing = false
                        return@QRScannerScreen
                    }

                    wearablesViewModel.pairDevice(pairingData) { success, message ->
                        pairingStatus = message
                        if (success) {
                            scope.launch {
                                delay(2000)
                                onDismiss()
                            }
                        } else {
                            isProcessing = false
                        }
                    }
                }
            },
            onDismiss = onDismiss
        )

        pairingStatus?.let { status ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (status.contains("success", ignoreCase = true))
                        Success else Error
                )
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
