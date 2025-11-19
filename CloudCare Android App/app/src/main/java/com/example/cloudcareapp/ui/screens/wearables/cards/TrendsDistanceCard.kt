package com.example.cloudcareapp.ui.screens.wearables.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Route
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
 * Trends Distance Card - Apple Health Style
 * 
 * Theme: Blue/Green (#00D4AA)
 * 
 * Layout:
 * - Header: Shows selected distance value (in km) and date
 * - Tabs: D (Day), W (Week), M (Month)
 * - Chart: InteractiveBarChart with blue/green theme
 * - Footer: Highlights text about distance covered
 */
@Composable
fun TrendsDistanceCard(
    data: List<AggregatedDataPoint>,
    onTimeframeChange: (timeframe: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val distanceColor = Color(0xFF00D4AA)
    var selectedTimeframe by remember { mutableStateOf("W") }
    var selectedPoint by remember { mutableStateOf<AggregatedDataPoint?>(null) }

    // Filter data based on timeframe
    val displayData = when (selectedTimeframe) {
        "D" -> data  // Show all hourly data points (24 hours)
        "W" -> data.takeLast(7)
        "M" -> data.takeLast(30)
        else -> data.takeLast(7)
    }

    // Calculate stats (data is in meters, convert to km)
    val totalDistance = displayData.sumOf { it.total / 1000.0 }
    val avgDistance = if (displayData.isNotEmpty()) (totalDistance / displayData.size) else 0.0
    val displayValue = selectedPoint?.let { it.total / 1000.0 } ?: totalDistance
    val displayDate = selectedPoint?.date ?: "Total"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
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
                                    distanceColor.copy(alpha = 0.2f),
                                    distanceColor.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Route,
                        contentDescription = null,
                        tint = distanceColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Distance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = displayDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Text(
                    text = String.format("%.2f", displayValue),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = distanceColor
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
                activeColor = distanceColor
            )

            // Interactive Bar Chart (convert meters to km for display)
            InteractiveBarChart(
                data = displayData.map { it.copy(total = it.total / 1000.0) },
                barColor = distanceColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                unit = "km",
                onPointSelected = { point ->
                    selectedPoint = point?.let { 
                        displayData.find { it.date == point.date }
                    }
                }
            )

            // Date labels
            if (displayData.isNotEmpty()) {
                DateLabels(dates = displayData.map { it.date })
            }

            // Highlights footer
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF2C2C2E)
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
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = generateDistanceInsight(avgDistance, totalDistance),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

// SegmentedControl and DateLabels now imported from CommonComponents.kt

/**
 * Generate insight text based on distance data
 */
private fun generateDistanceInsight(avgDistance: Double, totalDistance: Double): String {
    return when {
        avgDistance >= 10.0 -> "Amazing! You've covered ${String.format("%.1f", totalDistance)} km. That's excellent for cardiovascular health."
        avgDistance >= 5.0 -> "Great work! You walked/ran ${String.format("%.1f", totalDistance)} km this period."
        avgDistance >= 2.0 -> "You covered ${String.format("%.1f", totalDistance)} km. Keep up the momentum!"
        else -> "Try to increase your daily distance for better health benefits."
    }
}
