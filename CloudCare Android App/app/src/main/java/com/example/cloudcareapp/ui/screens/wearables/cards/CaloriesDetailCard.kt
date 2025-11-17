@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cloudcareapp.ui.screens.wearables.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.data.model.AggregatedDataPoint
import com.example.cloudcareapp.ui.screens.wearables.CardEmptyState
import com.example.cloudcareapp.ui.theme.CaloriesColor
import com.example.cloudcareapp.ui.theme.Surface
import com.example.cloudcareapp.ui.theme.TextSecondary

@Composable
fun CaloriesDetailCard(
    calorieMetrics: List<AggregatedDataPoint>?,
    modifier: Modifier = Modifier
) {
    if (calorieMetrics.isNullOrEmpty()) {
        CardEmptyState(
            message = "No calorie data available yet",
            modifier = modifier
        )
        return
    }

    val todayCalories = calorieMetrics.firstOrNull()?.total?.toInt() ?: 0
    val avgCalories = calorieMetrics.map { it.avg }.average().toInt()
    val maxCalories = calorieMetrics.maxOfOrNull { it.max }?.toInt() ?: 0
    val calorieGoal = 2000

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
                            .size(40.dp)
                            .background(CaloriesColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Calories",
                            tint = CaloriesColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Energy Burned",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${calorieMetrics.size} days tracked",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
                
                Text(
                    text = "$todayCalories",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = CaloriesColor
                )
            }
            
            // Goal progress bar
            val progressPercent = (todayCalories.toFloat() / calorieGoal).coerceIn(0f, 1f)
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Goal: $calorieGoal kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${(progressPercent * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = CaloriesColor
                    )
                }
                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = CaloriesColor,
                    trackColor = Color.Gray.copy(alpha = 0.1f)
                )
            }
            
            // Stats grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalorieStatItem(
                    label = "Avg",
                    value = "$avgCalories kcal",
                    modifier = Modifier.weight(1f)
                )
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp),
                    color = Color.Gray.copy(alpha = 0.2f)
                )
                CalorieStatItem(
                    label = "Max",
                    value = "$maxCalories kcal",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CalorieStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = CaloriesColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
