@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cloudcareapp.ui.screens.wearables.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.data.model.AggregatedDataPoint
import com.example.cloudcareapp.ui.screens.wearables.CardEmptyState
import com.example.cloudcareapp.ui.theme.HeartRateColor
import com.example.cloudcareapp.ui.theme.Surface
import com.example.cloudcareapp.ui.theme.TextSecondary

@Composable
fun HeartRateDetailCard(
    heartRateMetrics: List<AggregatedDataPoint>?,
    modifier: Modifier = Modifier
) {
    if (heartRateMetrics.isNullOrEmpty()) {
        CardEmptyState(
            message = "No heart rate data available yet",
            modifier = modifier
        )
        return
    }

    val avgHeartRate = heartRateMetrics.map { it.avg }.average().toInt()
    val minHeartRate = heartRateMetrics.minOfOrNull { it.min }?.toInt() ?: 0
    val maxHeartRate = heartRateMetrics.maxOfOrNull { it.max }?.toInt() ?: 0
    val latestHeartRate = heartRateMetrics.firstOrNull()?.avg?.toInt() ?: 0

    val heartRateStatus = when {
        latestHeartRate == 0 -> "No data"
        latestHeartRate < 60 -> "Low"
        latestHeartRate > 100 -> "Elevated"
        else -> "Normal"
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
                            .size(40.dp)
                            .background(HeartRateColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Heart Rate",
                            tint = HeartRateColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Heart Rate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = heartRateStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                Text(
                    text = "$latestHeartRate bpm",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = HeartRateColor
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
                HeartRateStatItem(
                    label = "Avg",
                    value = avgHeartRate.toString(),
                    modifier = Modifier.weight(1f)
                )
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp),
                    color = Color.Gray.copy(alpha = 0.2f)
                )
                HeartRateStatItem(
                    label = "Min",
                    value = minHeartRate.toString(),
                    modifier = Modifier.weight(1f)
                )
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp),
                    color = Color.Gray.copy(alpha = 0.2f)
                )
                HeartRateStatItem(
                    label = "Max",
                    value = maxHeartRate.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Information text
            Text(
                text = "Resting heart rate ranges from 60-100 bpm. Regular exercise can lower your resting heart rate.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun HeartRateStatItem(
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = HeartRateColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
