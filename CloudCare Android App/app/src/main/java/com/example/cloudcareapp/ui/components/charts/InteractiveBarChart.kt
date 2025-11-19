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
import androidx.compose.ui.unit.sp
import com.example.cloudcareapp.data.model.AggregatedDataPoint

/**
 * Interactive Bar Chart for Steps, Calories, Distance
 * 
 * Features:
 * - Vertical bars with rounded tops
 * - Gradient fill (bright to darker)
 * - Tap/drag interaction to highlight bars
 * - Selected bar shown at full opacity, others at 0.5 alpha
 * - Callback returns the selected data point
 * 
 * Apple Health Style:
 * - Dark mode compatible
 * - Smooth animations
 * - Pixel-perfect rounded bars
 */
@Composable
fun InteractiveBarChart(
    data: List<AggregatedDataPoint>,
    barColor: Color,
    modifier: Modifier = Modifier,
    showValues: Boolean = true,
    unit: String = "",
    onPointSelected: (AggregatedDataPoint?) -> Unit = {}
) {
    if (data.isEmpty()) {
        EmptyChartState(modifier = modifier)
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxValue = data.maxOfOrNull { it.total } ?: 1.0

    // Animated values for smooth entrance
    val animatedValues = data.map { dataPoint ->
        val animatedValue = remember { Animatable(0f) }
        LaunchedEffect(dataPoint.total) {
            animatedValue.animateTo(
                targetValue = (dataPoint.total / maxValue).toFloat(),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        animatedValue.value
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data) {
                    detectTapGestures(
                        onPress = { offset ->
                            // Robust hit testing
                            val barWidth = size.width / data.size
                            val index = (offset.x / barWidth).toInt().coerceIn(0, data.lastIndex)
                            
                            // Toggle selection: if clicking same bar, deselect
                            if (selectedIndex == index) {
                                selectedIndex = null
                                onPointSelected(null)
                            } else {
                                selectedIndex = index
                                onPointSelected(data[index])
                            }
                        }
                    )
                }
        ) {
            val barCount = data.size
            val spacing = 12.dp.toPx()
            val totalSpacing = spacing * (barCount - 1)
            val barWidth = (size.width - totalSpacing) / barCount
            val maxHeight = size.height

            // Draw subtle grid lines
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = maxHeight * (i.toFloat() / gridLines)
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw bars with Apple Health style
            animatedValues.forEachIndexed { index, animatedHeight ->
                val normalizedHeight = animatedHeight
                val x = index * (barWidth + spacing)
                val barHeight = maxHeight * normalizedHeight
                val y = maxHeight - barHeight

                // Determine opacity based on selection
                val isSelected = selectedIndex == index
                val barAlpha = if (selectedIndex == null || isSelected) 1.0f else 0.5f

                // Create rounded top rectangle path
                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(
                                left = x,
                                top = y,
                                right = x + barWidth,
                                bottom = maxHeight
                            ),
                            topLeft = CornerRadius(barWidth / 2, barWidth / 2),
                            topRight = CornerRadius(barWidth / 2, barWidth / 2)
                        )
                    )
                }

                // Gradient fill - bright at top, darker at bottom
                val gradient = Brush.verticalGradient(
                    colors = listOf(
                        barColor.copy(alpha = barAlpha),
                        barColor.copy(alpha = barAlpha * 0.7f)
                    ),
                    startY = y,
                    endY = maxHeight
                )

                drawPath(
                    path = path,
                    brush = gradient
                )
            }
        }

        // Show selected value overlay
        selectedIndex?.let { index ->
            if (index in data.indices) {
                val value = data[index].total.toInt()
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = barColor,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = if (unit.isNotEmpty()) "$value $unit" else "$value",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Empty state for charts with no data
 */
@Composable
private fun EmptyChartState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No data available",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

/**
 * Data point wrapper for simpler use cases
 */
data class ChartDataPoint(
    val label: String,
    val value: Double
)
