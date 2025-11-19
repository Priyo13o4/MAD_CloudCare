package com.example.cloudcareapp.ui.components.charts

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.data.model.SleepSession
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/**
 * Interactive Sleep Chart for Sleep Stages
 * 
 * Features:
 * - "Floating" stacked bars showing sleep stages over time
 * - Y-axis represents Time of Day (e.g., 6 PM to 10 AM next day)
 * - Uses actual start_time and end_time from sleep sessions
 * - NO hardcoded percentages - uses real time data
 * - Each stage has distinct color: Deep, Core, REM, Awake
 * - Tap interaction to show sleep details
 * 
 * Apple Health Style:
 * - Deep Blue (#3B2FEB) for Deep sleep
 * - Core Blue (#4F8EF7) for Core sleep
 * - REM Cyan (#7CC0FF) for REM sleep
 * - Awake Orange (#FE9F41) for Awake periods
 */
@Composable
fun InteractiveSleepChart(
    sleepSessions: List<SleepSession>,
    modifier: Modifier = Modifier,
    onSessionSelected: (SleepSession?) -> Unit = {}
) {
    if (sleepSessions.isEmpty()) {
        EmptyChartState(modifier = modifier)
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Sleep stage colors (Apple Health style)
    val deepColor = Color(0xFF3B2FEB)
    val coreColor = Color(0xFF4F8EF7)
    val remColor = Color(0xFF7CC0FF)
    val awakeColor = Color(0xFFFE9F41)

    // Parse all sessions to find time bounds
    val timeFormatter = DateTimeFormatter.ISO_DATE_TIME
    val sessionTimes = sleepSessions.mapNotNull { session ->
        try {
            val start = LocalDateTime.parse(session.start_time, timeFormatter)
            val end = LocalDateTime.parse(session.end_time, timeFormatter)
            Triple(session, start, end)
        } catch (e: Exception) {
            null
        }
    }

    if (sessionTimes.isEmpty()) {
        EmptyChartState(modifier = modifier)
        return
    }

    // Calculate time bounds (earliest start, latest end)
    val minTime = sessionTimes.minOf { it.second }
    val maxTime = sessionTimes.maxOf { it.third }
    val totalMinutes = java.time.Duration.between(minTime, maxTime).toMinutes().toDouble()

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Determine which session was tapped
                        val barWidth = size.width * 0.6f // Bar takes 60% of width
                        val barX = (size.width - barWidth) / 2

                        if (offset.x >= barX && offset.x <= barX + barWidth) {
                            // Within bar bounds - determine which session
                            sessionTimes.forEachIndexed { index, (session, start, end) ->
                                val startMinutes =
                                    java.time.Duration.between(minTime, start).toMinutes()
                                val endMinutes = java.time.Duration.between(minTime, end).toMinutes()

                                val yStart = (startMinutes / totalMinutes) * size.height
                                val yEnd = (endMinutes / totalMinutes) * size.height

                                if (offset.y >= yStart && offset.y <= yEnd) {
                                    selectedIndex = index
                                    onSessionSelected(session)
                                    return@detectTapGestures
                                }
                            }
                        }
                        // Click outside - deselect
                        selectedIndex = null
                        onSessionSelected(null)
                    }
                }
        ) {
            val barWidth = size.width * 0.6f // Center bar takes 60% width
            val barX = (size.width - barWidth) / 2

            // Draw time axis labels
            val hourFormatter = DateTimeFormatter.ofPattern("h a")
            val labelCount = 5
            for (i in 0..labelCount) {
                val fraction = i.toFloat() / labelCount
                val y = size.height * fraction
                val time = minTime.plusMinutes((totalMinutes * fraction).toLong())
                val label = time.format(hourFormatter)

                // Draw tick mark and label would go here
                // (Simplified for now - focus on bars)
            }

            // Draw each sleep session as stacked segments
            sessionTimes.forEachIndexed { index, (session, start, end) ->
                val isSelected = selectedIndex == index
                val alpha = if (selectedIndex == null || isSelected) 1.0f else 0.5f

                val startMinutes = java.time.Duration.between(minTime, start).toMinutes()
                val endMinutes = java.time.Duration.between(minTime, end).toMinutes()

                val yStart = (startMinutes / totalMinutes) * size.height
                val yEnd = (endMinutes / totalMinutes) * size.height
                val sessionHeight = yEnd - yStart

                // Get sleep stages for this session
                val stages = session.stages
                if (stages != null) {
                    val totalStageHours = stages.deep + stages.core + stages.rem + stages.awake
                    if (totalStageHours > 0) {
                        var currentY = yStart.toFloat()

                        // Draw each stage proportionally
                        listOf(
                            stages.deep to deepColor,
                            stages.core to coreColor,
                            stages.rem to remColor,
                            stages.awake to awakeColor
                        ).forEach { (hours, color) ->
                            if (hours > 0) {
                                val stageFraction = hours / totalStageHours
                                val stageHeight = sessionHeight * stageFraction

                                val path = Path().apply {
                                    addRoundRect(
                                        RoundRect(
                                            rect = Rect(
                                                left = barX,
                                                top = currentY,
                                                right = barX + barWidth,
                                                bottom = currentY + stageHeight.toFloat()
                                            ),
                                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                        )
                                    )
                                }

                                val gradient = Brush.verticalGradient(
                                    colors = listOf(
                                        color.copy(alpha = alpha),
                                        color.copy(alpha = alpha * 0.85f)
                                    ),
                                    startY = currentY,
                                    endY = currentY + stageHeight.toFloat()
                                )

                                drawPath(path = path, brush = gradient)
                                currentY += stageHeight.toFloat()
                            }
                        }
                    }
                } else {
                    // No stage data - draw as single bar
                    val path = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(
                                    left = barX,
                                    top = yStart.toFloat(),
                                    right = barX + barWidth,
                                    bottom = yEnd.toFloat()
                                ),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        )
                    }

                    drawPath(
                        path = path,
                        color = coreColor.copy(alpha = alpha)
                    )
                }
            }
        }

        // Show selected session info
        selectedIndex?.let { index ->
            if (index in sessionTimes.indices) {
                val (session, start, end) = sessionTimes[index]
                val duration = session.asleep_hours ?: session.in_bed_hours ?: 0.0

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1C1C1E),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format("%.1f h asleep", duration),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${start.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${
                                end.format(
                                    DateTimeFormatter.ofPattern("h:mm a")
                                )
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Empty state for sleep chart
 */
@Composable
private fun EmptyChartState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No sleep data available",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}
