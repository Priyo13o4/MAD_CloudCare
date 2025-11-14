package com.example.cloudcareapp.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.ui.theme.*

@Composable
fun LoginSelectionScreen(
    onPatientLoginClick: () -> Unit,
    onDoctorLoginClick: () -> Unit,
    onHospitalLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo and Title
            Icon(
                imageVector = Icons.Filled.LocalHospital,
                contentDescription = "CloudCare Logo",
                modifier = Modifier.size(80.dp),
                tint = Primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "CloudCare",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Text(
                text = "Healthcare Management System",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Login as",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login Type Cards
            LoginTypeCard(
                title = "Patient",
                description = "Access your health records and appointments",
                icon = Icons.Filled.Person,
                gradientColors = listOf(Primary, Primary.copy(alpha = 0.7f)),
                onClick = onPatientLoginClick
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LoginTypeCard(
                title = "Doctor",
                description = "Manage patients and view medical records",
                icon = Icons.Filled.MedicalServices,
                gradientColors = listOf(Secondary, Secondary.copy(alpha = 0.7f)),
                onClick = onDoctorLoginClick
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LoginTypeCard(
                title = "Hospital",
                description = "Hospital administration and resource management",
                icon = Icons.Filled.LocalHospital,
                gradientColors = listOf(Success, Success.copy(alpha = 0.7f)),
                onClick = onHospitalLoginClick
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Footer
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTypeCard(
    title: String,
    description: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.horizontalGradient(gradientColors)
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2
                    )
                }
                
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Navigate",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
