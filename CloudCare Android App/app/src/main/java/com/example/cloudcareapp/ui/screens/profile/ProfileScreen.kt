package com.example.cloudcareapp.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header
            item {
                ProfileHeader()
            }
            
            // Personal Information Section
            item {
                SectionTitle("Personal Information")
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Primary.copy(alpha = 0.1f)
                    ),
                    onClick = onEditProfileClick
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = Primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Edit Profile Information",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = Primary
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                ProfileInfoCard(
                    icon = Icons.Filled.Person,
                    label = "Full Name",
                    value = "Mobile Test User"
                )
            }
            
            item {
                ProfileInfoCard(
                    icon = Icons.Filled.Cake,
                    label = "Age",
                    value = "28 years"
                )
            }
            
            item {
                ProfileInfoCard(
                    icon = Icons.Filled.Wc,
                    label = "Gender",
                    value = "Male"
                )
            }
            
            item {
                ProfileInfoCard(
                    icon = Icons.Filled.Bloodtype,
                    label = "Blood Type",
                    value = "O+"
                )
            }
            
            // Contact Section
            item {
                SectionTitle("Contact Details")
            }
            
            item {
                ProfileInfoCard(
                    icon = Icons.Filled.Phone,
                    label = "Phone",
                    value = "+91 98765 43210"
                )
            }
            
            item {
                ProfileInfoCard(
                    icon = Icons.Filled.Email,
                    label = "Email",
                    value = "patient7@cloudcare.local"
                )
            }
            
            // Settings Section
            item {
                SectionTitle("Settings")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Notifications,
                    title = "Notifications",
                    onClick = onNavigateToNotifications
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Security,
                    title = "Privacy & Security",
                    onClick = onNavigateToPrivacy
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Help,
                    title = "Help & Support",
                    onClick = onNavigateToHelp
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "About",
                    onClick = onNavigateToAbout
                )
            }
            
            // Logout Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Mobile Test User",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Text(
                text = "Patient ID: 7",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Chip(
                    text = "28 years"
                )
                Chip(
                    text = "Male"
                )
                Chip(
                    text = "O+"
                )
            }
        }
    }
}

@Composable
fun Chip(text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Primary.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun ProfileInfoCard(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}
