package com.example.cloudcareapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.ui.theme.*

/**
 * Reusable Navigation Drawer Content
 * Can be used in ModalNavigationDrawer across the app
 */
@Composable
fun NavigationDrawerContent(
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "CloudCare Menu",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Divider()
        Spacer(modifier = Modifier.height(16.dp))
        
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text("Settings") },
            selected = false,
            onClick = onSettingsClick
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Help, contentDescription = null) },
            label = { Text("Help & Support") },
            selected = false,
            onClick = onHelpClick
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Info, contentDescription = null) },
            label = { Text("About") },
            selected = false,
            onClick = onAboutClick
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Logout, contentDescription = null) },
            label = { Text("Logout") },
            selected = false,
            onClick = onLogoutClick
        )
    }
}

/**
 * Reusable Top Navigation Bar with common action buttons
 * Supports QR scanner, notifications, profile, and settings menu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopAppBar(
    title: String,
    onMenuClick: (() -> Unit)? = null,
    onQRScanClick: (() -> Unit)? = null,
    notificationCount: Int = 0,
    onNotificationClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    showQRScanner: Boolean = true,
    showNotifications: Boolean = true,
    showProfile: Boolean = true,
    showSettingsMenu: Boolean = false,
    backgroundColor: Color = DoctorPrimary,
    onSettingsClick: (() -> Unit)? = null,
    onScheduleClick: (() -> Unit)? = null,
    onAnalyticsClick: (() -> Unit)? = null,
    onLogoutClick: (() -> Unit)? = null,
    isDoctorTheme: Boolean = true
) {
    var showMenu by remember { mutableStateOf(false) }
    
    TopAppBar(
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                if (showQRScanner && onQRScanClick != null) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = "QR Scanner Available",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        navigationIcon = {
            if (onMenuClick != null) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }
            }
        },
        actions = {
            // QR Scanner Button
            if (showQRScanner && onQRScanClick != null) {
                IconButton(onClick = onQRScanClick) {
                    Icon(
                        Icons.Filled.QrCodeScanner,
                        contentDescription = "Scan Patient QR",
                        tint = Color.White
                    )
                }
            }
            
            // Notifications Button
            if (showNotifications && onNotificationClick != null) {
                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge { Text(notificationCount.toString()) }
                        }
                    }
                ) {
                    IconButton(onClick = onNotificationClick) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                }
            }
            
            // Profile Button
            if (showProfile && onProfileClick != null) {
                IconButton(onClick = onProfileClick) {
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = "Profile",
                        tint = Color.White
                    )
                }
            }
            
            // Settings Menu Button
            if (showSettingsMenu) {
                Box {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "Settings Menu",
                            tint = Color.White
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        offset = DpOffset(0.dp, 8.dp),
                        modifier = Modifier.width(220.dp)
                    ) {
                        if (isDoctorTheme) {
                            // Doctor Menu Items
                            if (onSettingsClick != null) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Settings,
                                                contentDescription = null,
                                                tint = DoctorSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text("Settings", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        onSettingsClick()
                                    }
                                )
                            }
                            
                            if (onScheduleClick != null) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Schedule,
                                                contentDescription = null,
                                                tint = DoctorSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text("Schedule", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        onScheduleClick()
                                    }
                                )
                            }
                            
                            if (onAnalyticsClick != null) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.BarChart,
                                                contentDescription = null,
                                                tint = DoctorSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text("Analytics", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        onAnalyticsClick()
                                    }
                                )
                            }
                            
                            if (onSettingsClick != null || onScheduleClick != null || onAnalyticsClick != null) {
                                Divider()
                            }
                        }
                        
                        if (onLogoutClick != null) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Logout,
                                            contentDescription = null,
                                            tint = Color.Red,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text("Logout", style = MaterialTheme.typography.bodyMedium, color = Color.Red)
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onLogoutClick()
                                }
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor
        )
    )
}
