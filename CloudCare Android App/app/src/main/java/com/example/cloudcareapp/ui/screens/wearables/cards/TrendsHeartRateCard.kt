package com.example.cloudcareapp.ui.screens.wearables.cards

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cloudcareapp.data.model.HeartRateTrendDataPoint
import com.example.cloudcareapp.ui.components.DateLabels
import com.example.cloudcareapp.ui.components.SegmentedControl

/**
 * Heart Rate Trends Card with Apple Health Range Chart
 * 
 * Features:
 * - Floating vertical bars showing min-to-max range
 * - Local filtering (D/W/M) without triggering global refresh
 * - Red/Orange gradient for heart rate visualization
 * - Tap to show detailed min/max/avg values
 * 
 * Apple Health Style:
 * - Y-Axis: 0-200 BPM (adaptive)
 * - Bars: Vertical capsules from min_bpm to max_bpm
 * - Grid: Dashed lines at 50, 100, 150 BPM
 */
@Composable
fun TrendsHeartRateCard(
    heartRateTrends: List<HeartRateTrendDataPoint>,
    onTimeframeChange: (timeframe: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf("W") }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    // Local filtering - does NOT trigger ViewModel refresh
    val filteredData = remember(heartRateTrends, selectedPeriod) {
        when (selectedPeriod) {
            "D" -> heartRateTrends  // Show all available data points
            "W" -> heartRateTrends.takeLast(7)
            "M" -> heartRateTrends.takeLast(30)
            else -> heartRateTrends.takeLast(7)
        }
    }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
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
                Column {
                    Text(
                        text = "Heart Rate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (selectedIndex != null && selectedIndex!! in filteredData.indices) {
                        val point = filteredData[selectedIndex!!]
                        Text(
                            text = "${point.bpm.toInt()} BPM • ${point.date}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    } else if (filteredData.isNotEmpty()) {
                        val avgBpm = filteredData.map { it.bpm }.average()
                        Text(
                            text = "Avg: ${avgBpm.toInt()} BPM",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                
                SegmentedControl(
                    options = listOf("D", "W", "M"),
                    selectedOption = selectedPeriod,
                    onOptionSelected = { period ->
                        selectedPeriod = period
                        selectedIndex = null
                        onTimeframeChange(period)
                    },
                    activeColor = Color(0xFFFF5151)
                )
            }
            
            // Chart Area
            if (filteredData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No heart rate data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                RangeHeartRateChart(
                    data = filteredData,
                    onPointSelected = { selectedIndex = it },
                    selectedIndex = selectedIndex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
            
            // Date Labels
            if (filteredData.isNotEmpty()) {
                DateLabels(
                    dates = filteredData.map { it.date },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Min/Max Range Display
            if (selectedIndex != null && selectedIndex!! in filteredData.indices) {
                val point = filteredData[selectedIndex!!]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Min",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${point.min_bpm.toInt()} BPM",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Avg",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${point.bpm.toInt()} BPM",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Max",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${point.max_bpm.toInt()} BPM",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * Apple Health Range Chart for Heart Rate
 * 
 * Visual Style:
 * - Y-Axis: 0-200 BPM (or adaptive based on data) with numeric labels
 * - Grid: Dashed lines at 50, 100, 150 BPM
 * - Bars: Vertical capsules from min_bpm to max_bpm
 * - Color: Red/Orange gradient
 * - Y-Axis Labels: 0, 50, 100, 150, 200 on the left side
 */
@Composable
fun RangeHeartRateChart(
    data: List<HeartRateTrendDataPoint>,
    selectedIndex: Int?,
    onPointSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        fontSize = 10.sp,
        color = Color.Gray,
        fontWeight = FontWeight.Normal
    )
    
    // Y-Axis scale: Adaptive or 0-200 BPM
    val minBpmData = data.minOf { it.min_bpm }
    val maxBpmData = data.maxOf { it.max_bpm }
    
    val yMin = 0.0
    val yMax = if (maxBpmData > 180) 200.0 else 180.0
    val yRange = yMax - yMin
    
    var selectedIndexInternal by remember { mutableStateOf<Int?>(null) }
    
    // Animation state
    val progress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
    }
    
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val barWidth = size.width / data.size
                        val index = (offset.x / barWidth).toInt().coerceIn(0, data.lastIndex)
                        
                        // Toggle selection
                        if (selectedIndexInternal == index) {
                            selectedIndexInternal = null
                            onPointSelected(null)
                        } else {
                            selectedIndexInternal = index
                            onPointSelected(index)
                        }
                    }
                }
        ) {
            val chartHeight = size.height
            val chartWidth = size.width
            val barWidth = chartWidth / data.size
            
            // Draw Y-axis labels (0, 50, 100, 150, 200 BPM)
            val yAxisLabelBpmValues = listOf(0.0, 50.0, 100.0, 150.0, 200.0)
            val labelPadding = 8f // padding from left edge
            
            yAxisLabelBpmValues.forEach { bpmValue ->
                if (bpmValue <= yMax) {
                    val normalizedY = (yMax - bpmValue) / yRange
                    val y = chartHeight * normalizedY.toFloat()
                    
                    // Draw label using drawText with textMeasurer
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "${bpmValue.toInt()}",
                        topLeft = Offset(labelPadding - 20f, y - 10f),
                        style = labelStyle
                    )
                }
            }
            
            // Draw dashed grid lines at 50, 100, 150 BPM
            val gridBpmValues = listOf(50.0, 100.0, 150.0)
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
            
            // Offset for chart area (to account for left margin for labels)
            val chartStartX = 25f
            val chartEndX = chartWidth
            val chartDisplayWidth = chartEndX - chartStartX
            
            gridBpmValues.forEach { bpmValue ->
                if (bpmValue <= yMax) {
                    val normalizedY = (yMax - bpmValue) / yRange
                    val y = chartHeight * normalizedY.toFloat()
                    
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(chartStartX, y),
                        end = Offset(chartEndX, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = pathEffect
                    )
                }
            }
            
            // Draw range bars (adjusted for left margin)
            data.forEachIndexed { index, point ->
                val x = chartStartX + index.toFloat() * (chartDisplayWidth / data.size) + (chartDisplayWidth / data.size * 0.25f)
                val effectiveBarWidth = (chartDisplayWidth / data.size) * 0.5f
                
                // Calculate Y positions for min and max BPM
                val minNormalized = (yMax - point.min_bpm) / yRange
                val maxNormalized = (yMax - point.max_bpm) / yRange
                
                val yMinPos = (chartHeight * minNormalized.toFloat()).coerceIn(0f, chartHeight)
                val yMaxPos = (chartHeight * maxNormalized.toFloat()).coerceIn(0f, chartHeight)
                
                // Bar height (from max to min BPM)
                val barHeight = kotlin.math.abs(yMinPos - yMaxPos) * progress.value
                val barTop = kotlin.math.min(yMinPos, yMaxPos)
                
                val isSelected = selectedIndexInternal == index || selectedIndex == index
                val barAlpha = if (selectedIndexInternal != null && !isSelected) 0.4f else 1.0f
                
                // Red/Orange gradient
                val brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF5151).copy(alpha = barAlpha),
                        Color(0xFFFF0000).copy(alpha = barAlpha)
                    ),
                    startY = barTop,
                    endY = barTop + barHeight
                )
                
                // Draw rounded capsule bar
                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(x, barTop),
                    size = Size(effectiveBarWidth, barHeight),
                    cornerRadius = CornerRadius(effectiveBarWidth / 2f, effectiveBarWidth / 2f)
                )
            }
        }
    }
}
