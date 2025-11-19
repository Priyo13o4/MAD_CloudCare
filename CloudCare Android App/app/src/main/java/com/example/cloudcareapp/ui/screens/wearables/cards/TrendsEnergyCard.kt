package com.example.cloudcareapp.ui.screens.wearables.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
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
 * Trends Energy (Calories) Card - Apple Health Style
 * 
 * Theme: Red/Orange Gradient (#FF2D55)
 * 
 * Layout:
 * - Header: Shows selected calorie value and date
 * - Tabs: D (Day), W (Week), M (Month)
 * - Chart: InteractiveBarChart with red/orange gradient
 * - Footer: Highlights text about calorie burn
 */
@Composable
fun TrendsEnergyCard(
    data: List<AggregatedDataPoint>,
    onTimeframeChange: (timeframe: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val caloriesColor = Color(0xFFFF2D55)
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
    val totalCalories = displayData.sumOf { it.total }.toInt()
    val avgCalories = if (displayData.isNotEmpty()) (totalCalories / displayData.size) else 0
    val displayValue = selectedPoint?.total?.toInt() ?: totalCalories
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
                                    caloriesColor.copy(alpha = 0.2f),
                                    caloriesColor.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = caloriesColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Energy",
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
                    text = "$displayValue",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = caloriesColor
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
                activeColor = caloriesColor
            )

            // Interactive Bar Chart
            InteractiveBarChart(
                data = displayData,
                barColor = caloriesColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                unit = "kcal",
                onPointSelected = { selectedPoint = it }
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
                        text = generateCaloriesInsight(avgCalories, totalCalories),
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
 * Generate insight text based on calories data
 */
private fun generateCaloriesInsight(avgCalories: Int, totalCalories: Int): String {
    return when {
        avgCalories >= 2500 -> "Excellent! You're burning more calories than the average daily goal."
        avgCalories >= 2000 -> "You burned $totalCalories kcal. You're on track with your energy expenditure."
        avgCalories >= 1500 -> "You burned $totalCalories kcal. Consider increasing physical activity."
        else -> "Try to increase your daily activity to burn more calories."
    }
}
