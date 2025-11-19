package com.example.cloudcareapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Reusable Segmented Control Component
 * 
 * Apple Health-style segmented control for timeframe selection
 * Used across all Trend Cards (Steps, Energy, Distance, Sleep)
 * 
 * @param options List of option labels (e.g., ["D", "W", "M"])
 * @param selectedOption Currently selected option
 * @param onOptionSelected Callback when option is tapped
 * @param activeColor Color for selected option background
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2C2C2E)),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) activeColor else Color.Transparent)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * Date Labels Component
 * 
 * Displays date labels under charts with smart spacing
 * Used across all Trend Cards
 * 
 * @param dates List of date strings (e.g., ["2025-11-15", "2025-11-16", ...])
 */
@Composable
fun DateLabels(
    dates: List<String>,
    modifier: Modifier = Modifier
) {
    if (dates.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // FIX: Ensure step is at least 1 to avoid DivideByZero exception
        val step = (dates.size / 6).coerceAtLeast(1)
        
        dates.forEachIndexed { index, date ->
            // Logic: Show first, last, and items matching the step
            if (index == 0 || index == dates.lastIndex || index % step == 0) {
                // Limit total labels to avoid overcrowding if logic allows too many
                val label = try {
                    date.split("-").last()
                } catch (e: Exception) {
                    date
                }
                
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(24.dp) // Fixed width for alignment
                )
            }
        }
    }
}

/**
 * Empty State Component for Cards
 * 
 * Displays when no data is available for a card
 * Shows an info icon and message
 * 
 * @param message The message to display
 */
@Composable
fun CardEmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = Color.Gray
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}
