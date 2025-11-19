@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cloudcareapp.ui.screens.wearables.cards

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cloudcareapp.data.model.AggregatedDataPoint
import com.example.cloudcareapp.data.model.SleepSession
import com.example.cloudcareapp.ui.components.CardEmptyState
import com.example.cloudcareapp.ui.theme.SleepColor
import com.example.cloudcareapp.ui.theme.Surface
import com.example.cloudcareapp.ui.theme.TextSecondary
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// Sleep stage colors matching Apple Health
private val AwakeSleepColor = Color(0xFFFF6B6B)  // Red for awake periods
private val DeepSleepColor = Color(0xFF4A148C)    // Purple for deep sleep
private val CoreSleepColor = Color(0xFF2196F3)    // Blue for core sleep
private val RemSleepColor = Color(0xFF00BCD4)     // Cyan for REM sleep

@Composable
fun SleepDetailCard(
    sleepMetrics: List<AggregatedDataPoint>?,
    todaySleepHours: Double,
    sleepTimeInBed: Double? = null,
    sleepTimeAsleep: Double? = null,
    sleepStages: com.example.cloudcareapp.data.model.SleepStages? = null,
    sleepSessions: List<SleepSession>? = null,
    modifier: Modifier = Modifier
) {
    if ((sleepSessions.isNullOrEmpty() && sleepMetrics.isNullOrEmpty()) || todaySleepHours == 0.0) {
        CardEmptyState(
            message = "No sleep data available yet",
            modifier = modifier
        )
        return
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SleepColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Nightlight,
                            contentDescription = "Sleep",
                            tint = SleepColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Sleep Duration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${sleepMetrics?.size ?: sleepSessions?.size ?: 0} days tracked",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
                
                Text(
                    text = String.format("%.1f h", sleepTimeAsleep ?: todaySleepHours),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleepColor
                )
            }
            
            // Time in Bed and Time Asleep display
            if (sleepTimeInBed != null || sleepTimeAsleep != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (sleepTimeInBed != null) {
                        SleepStatItem(
                            label = "TIME IN BED",
                            value = String.format("%.0f hr %.0f min", 
                                sleepTimeInBed.toInt().toDouble(),
                                ((sleepTimeInBed - sleepTimeInBed.toInt()) * 60))
                        )
                    }
                    if (sleepTimeAsleep != null) {
                        SleepStatItem(
                            label = "TIME ASLEEP",
                            value = String.format("%.0f hr %.0f min",
                                sleepTimeAsleep.toInt().toDouble(),
                                ((sleepTimeAsleep - sleepTimeAsleep.toInt()) * 60))
                        )
                    }
                }
            }
            
            // Sleep stages breakdown
            if (sleepStages != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Stages",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // Calculate total hours and percentages
                    val totalHours = sleepStages.awake + sleepStages.rem + sleepStages.core + sleepStages.deep
                    
                    if (totalHours > 0) {
                        // Horizontal stacked bar showing all stages
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                // Deep sleep
                                if (sleepStages.deep > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight((sleepStages.deep / totalHours).toFloat())
                                            .background(DeepSleepColor)
                                    )
                                }
                                // Core sleep
                                if (sleepStages.core > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight((sleepStages.core / totalHours).toFloat())
                                            .background(CoreSleepColor)
                                    )
                                }
                                // REM sleep
                                if (sleepStages.rem > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight((sleepStages.rem / totalHours).toFloat())
                                            .background(RemSleepColor)
                                    )
                                }
                                // Awake time
                                if (sleepStages.awake > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight((sleepStages.awake / totalHours).toFloat())
                                            .background(AwakeSleepColor)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Stage details with durations
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (sleepStages.awake > 0) {
                                SleepStageRow(
                                    label = "Awake",
                                    hours = sleepStages.awake,
                                    color = AwakeSleepColor
                                )
                            }
                            if (sleepStages.rem > 0) {
                                SleepStageRow(
                                    label = "REM",
                                    hours = sleepStages.rem,
                                    color = RemSleepColor
                                )
                            }
                            if (sleepStages.core > 0) {
                                SleepStageRow(
                                    label = "Core",
                                    hours = sleepStages.core,
                                    color = CoreSleepColor
                                )
                            }
                            if (sleepStages.deep > 0) {
                                SleepStageRow(
                                    label = "Deep",
                                    hours = sleepStages.deep,
                                    color = DeepSleepColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SleepStageRow(
    label: String,
    hours: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = String.format("%.0f hr %.0f min", 
                hours.toInt().toDouble(),
                ((hours - hours.toInt()) * 60)),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SleepStageBar(
    label: String,
    percentage: Float,
    color: Color,
    hours: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(percentage)
            .background(color, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (percentage > 0.12f) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun SleepStatItem(
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
            color = SleepColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun SleepStageIndicator(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
