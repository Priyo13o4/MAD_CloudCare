package com.example.cloudcareapp.ui.screens.wearables.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.data.model.SleepStages
import com.example.cloudcareapp.data.model.SleepTrendDataPoint
import com.example.cloudcareapp.ui.components.SegmentedControl
import com.example.cloudcareapp.ui.components.DateLabels

/**
 * Trends Sleep Card - Honest Sleep Data Visualization
 * 
 * Theme: Purple (#8B5CF6)
 * 
 * Uses the dedicated /metrics/sleep-trends endpoint which correctly separates:
 * - time_in_bed: From 'inBed' samples (background bar, 30% alpha)
 * - time_asleep: From 'core' + 'deep' + 'rem' samples (foreground bar, 100% alpha)
 * 
 * NO HARDCODED PERCENTAGES - Shows only real data from backend.
 */
@Composable
fun TrendsSleepCard(
    sleepTrends: List<SleepTrendDataPoint>,
    dailySleepStages: SleepStages? = null,
    onTimeframeChange: (timeframe: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sleepColor = Color(0xFF8B5CF6)
    var selectedTimeframe by remember { mutableStateOf("W") }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Local filtering - does NOT trigger ViewModel refresh
    val displayData = remember(sleepTrends, selectedTimeframe) {
        when (selectedTimeframe) {
            "D" -> emptyList() // Handled by dailySleepStages
            "W" -> sleepTrends.takeLast(7)
            "M" -> sleepTrends.takeLast(30)
            else -> sleepTrends.takeLast(7)
        }
    }

    // Calculate summary statistics
    val avgSleepAsleep = if (displayData.isNotEmpty()) {
        displayData.map { it.time_asleep }.average()
    } else 0.0
    
    val avgSleepEfficiency = if (displayData.isNotEmpty()) {
        displayData.map { it.sleepEfficiency }.average()
    } else 0.0

    // Display value: selected bar or average
    val selectedData = selectedIndex?.let { displayData.getOrNull(it) }
    val displaySleepAsleep = selectedData?.time_asleep ?: avgSleepAsleep
    val displayDate = selectedData?.date ?: "Average"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Nightlight,
                        contentDescription = "Sleep",
                        tint = sleepColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Sleep",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (selectedTimeframe == "D") "Last Night" else displayDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Display value
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (selectedTimeframe == "D" && dailySleepStages != null) {
                            val total = dailySleepStages.core + dailySleepStages.deep + dailySleepStages.rem
                            String.format("%.1fh", total)
                        } else {
                            String.format("%.1fh", displaySleepAsleep)
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = sleepColor
                    )
                    Text(
                        text = "asleep",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // Segmented Control (FIXED SIGNATURE)
            SegmentedControl(
                options = listOf("D", "W", "M"),
                selectedOption = selectedTimeframe,
                onOptionSelected = { 
                    selectedTimeframe = it
                    selectedIndex = null // Reset selection
                    onTimeframeChange(it)
                },
                activeColor = sleepColor
            )

            if (selectedTimeframe == "D" && dailySleepStages != null) {
                // Daily Breakdown View
                DailySleepBreakdown(
                    stages = dailySleepStages,
                    sleepColor = sleepColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                // Honest Sleep Bar Chart
                HonestSleepBarChart(
                    data = displayData,
                    selectedIndex = selectedIndex,
                    onBarSelected = { selectedIndex = it },
                    sleepColor = sleepColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                // Date Labels (FIXED SIGNATURE)
                if (displayData.isNotEmpty()) {
                    DateLabels(dates = displayData.map { it.date })
                }
            }

            // Footer Insights
            Text(
                text = if (selectedTimeframe == "D") {
                    "Breakdown of sleep stages for last night."
                } else if (displayData.isNotEmpty()) {
                    when {
                        avgSleepEfficiency >= 90 -> "Excellent sleep efficiency! You're getting quality rest."
                        avgSleepEfficiency >= 80 -> "Good sleep efficiency. Most time in bed is spent sleeping."
                        avgSleepEfficiency >= 70 -> "Fair sleep efficiency. Consider improving sleep hygiene."
                        else -> "Low sleep efficiency. Time in bed doesn't match actual sleep."
                    }
                } else {
                    "No sleep data available for this period."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun DailySleepBreakdown(
    stages: SleepStages,
    sleepColor: Color,
    modifier: Modifier = Modifier
) {
    val total = stages.awake + stages.rem + stages.core + stages.deep
    if (total == 0.0) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No sleep data for last night", color = Color.Gray)
        }
        return
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        // Simple stacked bar or list of stages
        // Let's do a list of stages with progress bars
        StageRow("Awake", stages.awake, total, Color(0xFFFF9F0A))
        Spacer(modifier = Modifier.height(8.dp))
        StageRow("REM", stages.rem, total, Color(0xFFBF5AF2))
        Spacer(modifier = Modifier.height(8.dp))
        StageRow("Core", stages.core, total, Color(0xFF5E5CE6))
        Spacer(modifier = Modifier.height(8.dp))
        StageRow("Deep", stages.deep, total, Color(0xFF0A84FF))
    }
}

@Composable
private fun StageRow(label: String, value: Double, total: Double, color: Color) {
    val percentage = if (total > 0) (value / total).toFloat() else 0f
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(50.dp)
        )
        
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        
        Text(
            text = String.format("%.1fh", value),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.width(40.dp)
        )
    }
}

/**
 * Honest Sleep Bar Chart - Dual-Bar System
 * 
 * Background Bar (30% alpha): Time in Bed (point.time_in_bed)
 * Foreground Bar (100% alpha): Actual Sleep (point.time_asleep)
 * 
 * This visualization shows sleep efficiency without fabricating stage data.
 */
@Composable
private fun HonestSleepBarChart(
    data: List<SleepTrendDataPoint>,
    selectedIndex: Int?,
    onBarSelected: (Int?) -> Unit,
    sleepColor: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No sleep data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        return
    }

    // Find max value for scaling (use time_in_bed as it's always >= time_asleep)
    val maxValue = data.maxOfOrNull { it.time_in_bed } ?: 10.0
    val chartMax = (maxValue * 1.2).coerceAtLeast(8.0) // At least 8 hours scale

    Canvas(
        modifier = modifier
            .pointerInput(data) {
                detectTapGestures { offset ->
                    val barWidth = size.width / data.size
                    val clickedIndex = (offset.x / barWidth).toInt()
                    
                    if (clickedIndex in data.indices) {
                        onBarSelected(if (selectedIndex == clickedIndex) null else clickedIndex)
                    }
                }
            }
    ) {
        val barWidth = size.width / data.size
        val spacing = barWidth * 0.2f
        val actualBarWidth = barWidth - spacing

        data.forEachIndexed { index, dataPoint ->
            val x = index * barWidth + spacing / 2

            // Calculate heights
            val timeInBedHeight = ((dataPoint.time_in_bed / chartMax) * size.height).toFloat()
            val timeAsleepHeight = ((dataPoint.time_asleep / chartMax) * size.height).toFloat()

            val isSelected = selectedIndex == index

            // Background bar: Time in Bed (dimmed)
            drawRoundRect(
                color = sleepColor.copy(alpha = if (isSelected) 0.5f else 0.3f),
                topLeft = Offset(x, size.height - timeInBedHeight),
                size = Size(actualBarWidth, timeInBedHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Foreground bar: Actual Sleep (solid)
            drawRoundRect(
                color = if (isSelected) sleepColor.copy(alpha = 1f) else sleepColor.copy(alpha = 0.9f),
                topLeft = Offset(x, size.height - timeAsleepHeight),
                size = Size(actualBarWidth, timeAsleepHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )
        }
    }
}
