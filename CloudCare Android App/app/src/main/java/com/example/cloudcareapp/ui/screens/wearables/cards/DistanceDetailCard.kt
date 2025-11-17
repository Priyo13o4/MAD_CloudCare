@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cloudcareapp.ui.screens.wearables.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.data.model.AggregatedDataPoint
import com.example.cloudcareapp.ui.screens.wearables.CardEmptyState
import com.example.cloudcareapp.ui.theme.StepsColor
import com.example.cloudcareapp.ui.theme.Surface
import com.example.cloudcareapp.ui.theme.TextSecondary

@Composable
fun DistanceDetailCard(
    distanceMetrics: List<AggregatedDataPoint>?,
    modifier: Modifier = Modifier
) {
    if (distanceMetrics.isNullOrEmpty()) {
        CardEmptyState(
            message = "No distance data available yet",
            modifier = modifier
        )
        return
    }

    val todayDistance = distanceMetrics.firstOrNull()?.total ?: 0.0
    val avgDistance = distanceMetrics.map { it.avg }.average()
    val maxDistance = distanceMetrics.maxOfOrNull { it.max } ?: 0.0
    val totalDistance = distanceMetrics.sumOf { it.total }

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
                            .background(StepsColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Route,
                            contentDescription = "Distance",
                            tint = StepsColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Distance Covered",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${distanceMetrics.size} days tracked",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
                
                Text(
                    text = String.format("%.2f km", todayDistance),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = StepsColor
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
                DistanceStatItem(
                    label = "Avg",
                    value = String.format("%.2f km", avgDistance),
                    modifier = Modifier.weight(1f)
                )
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp),
                    color = Color.Gray.copy(alpha = 0.2f)
                )
                DistanceStatItem(
                    label = "Max",
                    value = String.format("%.2f km", maxDistance),
                    modifier = Modifier.weight(1f)
                )
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp),
                    color = Color.Gray.copy(alpha = 0.2f)
                )
                DistanceStatItem(
                    label = "Total",
                    value = String.format("%.2f km", totalDistance),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DistanceStatItem(
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
            color = StepsColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
