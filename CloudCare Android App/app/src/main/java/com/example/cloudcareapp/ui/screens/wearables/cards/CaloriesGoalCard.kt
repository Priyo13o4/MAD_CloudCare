package com.example.cloudcareapp.ui.screens.wearables.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.data.model.HealthSummary

// Color constants
private val CaloriesColor = Color(0xFFFF2D55)
private val StepsColor = Color(0xFFFF6B00)
private val SleepColor = Color(0xFF8B5CF6)

@Composable
fun CaloriesGoalCard(
    summary: HealthSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))  // Light mode
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Today's Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black  // Light mode
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Concentric rings
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                ConcentricRings(
                    caloriesProgress = summary.calories.toFloat() / summary.caloriesGoal.toFloat().coerceAtLeast(1f),
                    stepsProgress = summary.steps.toFloat() / 10000f,
                    sleepProgress = summary.sleepHours.toFloat() / 8f,
                    modifier = Modifier.fillMaxSize()
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${summary.calories}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = CaloriesColor
                    )
                    Text(
                        text = "kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricLegendItem(
                    color = CaloriesColor,
                    label = "Move",
                    value = "${summary.calories}/${summary.caloriesGoal}"
                )
                MetricLegendItem(
                    color = StepsColor,
                    label = "Steps",
                    value = "${summary.steps}/10k"
                )
                MetricLegendItem(
                    color = SleepColor,
                    label = "Sleep",
                    value = String.format("%.1fh", summary.sleepHours)
                )
            }
        }
    }
}

@Composable
fun ConcentricRings(
    caloriesProgress: Float,
    stepsProgress: Float,
    sleepProgress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 18.dp.toPx()
        val spacing = 8.dp.toPx()

        // Outer ring - Calories
        val caloriesRadius = (size.minDimension / 2) - strokeWidth / 2
        drawRing(CaloriesColor, caloriesProgress, caloriesRadius, strokeWidth)

        // Middle ring - Steps
        val stepsRadius = caloriesRadius - strokeWidth - spacing
        drawRing(StepsColor, stepsProgress, stepsRadius, strokeWidth)

        // Inner ring - Sleep
        val sleepRadius = stepsRadius - strokeWidth - spacing
        drawRing(SleepColor, sleepProgress, sleepRadius, strokeWidth)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRing(
    color: Color,
    progress: Float,
    radius: Float,
    strokeWidth: Float
) {
    // Background track
    drawArc(
        color = color.copy(alpha = 0.15f),
        startAngle = -90f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    // Progress arc
    drawArc(
        brush = Brush.sweepGradient(
            colors = listOf(color, color.copy(alpha = 0.7f))
        ),
        startAngle = -90f,
        sweepAngle = 360f * progress.coerceIn(0f, 1f),
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

@Composable
fun MetricLegendItem(
    color: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black  // Light mode
        )
    }
}
