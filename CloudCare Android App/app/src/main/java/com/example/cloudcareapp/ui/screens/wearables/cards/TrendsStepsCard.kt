package com.example.cloudcareapp.ui.screens.wearables.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.data.model.AggregatedDataPoint
import com.example.cloudcareapp.ui.components.charts.InteractiveBarChart
import com.example.cloudcareapp.ui.components.SegmentedControl
import com.example.cloudcareapp.ui.components.DateLabels

/**
 * Trends Steps Card - Apple Health Style
 * 
 * Theme: Orange (#FF6B00)
 * 
 * Layout:
 * - Header: Shows selected value and date
 * - Tabs: D (Day), W (Week), M (Month)
 * - Chart: InteractiveBarChart with orange theme
 * - Footer: Highlights text (e.g., "You walked more than usual...")
 */
@Composable
fun TrendsStepsCard(
    data: List<AggregatedDataPoint>,
    onTimeframeChange: (timeframe: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val stepsColor = Color(0xFFFF6B00)
    var selectedTimeframe by remember { mutableStateOf("W") }
    var selectedPoint by remember { mutableStateOf<AggregatedDataPoint?>(null) }

    // Filter data based on timeframe
    val displayData = when (selectedTimeframe) {
        "D" -> data  // Show all hourly data points (24 hours)
        "W" -> data.takeLast(7)
        "M" -> data.takeLast(30)
        else -> data.takeLast(7)
    }

    // Calculate stats
    val totalSteps = displayData.sumOf { it.total }.toInt()
    val avgSteps = if (displayData.isNotEmpty()) (totalSteps / displayData.size) else 0
    val displayValue = selectedPoint?.total?.toInt() ?: totalSteps
    val displayDate = selectedPoint?.date ?: "Total"

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
            // Header with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                                    stepsColor.copy(alpha = 0.2f),
                                    stepsColor.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint = stepsColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Steps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Text(
                    text = "$displayValue",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = stepsColor
                )
            }

            // Timeframe tabs
            SegmentedControl(
                options = listOf("D", "W", "M"),
                selectedOption = selectedTimeframe,
                onOptionSelected = { 
                    selectedTimeframe = it
                    onTimeframeChange(it)
                },
                activeColor = stepsColor
            )

            // Interactive Bar Chart
            InteractiveBarChart(
                data = displayData,
                barColor = stepsColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                unit = "steps",
                onPointSelected = { selectedPoint = it }
            )

            // Date labels
            if (displayData.isNotEmpty()) {
                DateLabels(dates = displayData.map { it.date })
            }

            // Highlights footer
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Highlights",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = generateStepsInsight(avgSteps, totalSteps),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// SegmentedControl and DateLabels now imported from CommonComponents.kt

/**
 * Generate insight text based on steps data
 */
private fun generateStepsInsight(avgSteps: Int, totalSteps: Int): String {
    return when {
        avgSteps >= 10000 -> "Great job! You're consistently hitting your daily goal of 10,000 steps."
        avgSteps >= 7000 -> "You're making good progress! Keep it up to reach 10,000 steps daily."
        avgSteps >= 5000 -> "You walked $totalSteps steps this period. Try to increase your daily average."
        else -> "Start with small goals and gradually increase your daily steps."
    }
}
