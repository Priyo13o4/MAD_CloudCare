package com.example.cloudcareapp.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cloudcareapp.ui.theme.Primary
import com.example.cloudcareapp.ui.theme.Secondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLoginSelection: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val alphaAnimation = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )
    
    val scaleAnimation = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 800),
        label = "scale"
    )
    
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500) // Show splash for 2.5 seconds
        onNavigateToLoginSelection()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Primary,
                        Secondary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alphaAnimation.value)
                .scale(scaleAnimation.value)
        ) {
            Icon(
                imageVector = Icons.Filled.LocalHospital,
                contentDescription = "CloudCare Logo",
                modifier = Modifier.size(120.dp),
                tint = Color.White
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "CloudCare",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Healthcare Management System",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Pulsing dot indicator
            PulsingDot()
        }
        
        // Version text at bottom
        Text(
            text = "Version 1.0.0",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(alphaAnimation.value),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = Modifier
            .size(12.dp)
            .scale(scale)
            .alpha(alpha)
            .background(Color.White, shape = androidx.compose.foundation.shape.CircleShape)
    )
}
