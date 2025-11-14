package com.example.cloudcareapp.ui.screens.notifications

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val timestamp: String,
    val type: NotificationType,
    val isRead: Boolean = false
)

enum class NotificationType {
    APPOINTMENT,
    CONSENT,
    RECORD,
    DEVICE,
    ALERT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val notifications = remember {
        listOf(
            Notification(
                id = 1,
                title = "New Consent Request",
                message = "Dr Lal Pathlabs is requesting access to your lab results",
                timestamp = "2 hours ago",
                type = NotificationType.CONSENT,
                isRead = false
            ),
            Notification(
                id = 2,
                title = "Upcoming Appointment",
                message = "You have an appointment with Dr. Suresh tomorrow at 10:00 AM",
                timestamp = "5 hours ago",
                type = NotificationType.APPOINTMENT,
                isRead = false
            ),
            Notification(
                id = 3,
                title = "Health Record Shared",
                message = "Your health record has been shared with Archana Eye Clinic",
                timestamp = "1 day ago",
                type = NotificationType.RECORD,
                isRead = true
            ),
            Notification(
                id = 4,
                title = "Device Synced",
                message = "Xiaomi Mi Band data synced successfully",
                timestamp = "1 day ago",
                type = NotificationType.DEVICE,
                isRead = true
            ),
            Notification(
                id = 5,
                title = "Consent Approved",
                message = "Your consent request to Apollo Hospital has been approved",
                timestamp = "2 days ago",
                type = NotificationType.CONSENT,
                isRead = true
            ),
            Notification(
                id = 6,
                title = "New Lab Report",
                message = "Your blood test results are now available",
                timestamp = "3 days ago",
                type = NotificationType.RECORD,
                isRead = true
            ),
            Notification(
                id = 7,
                title = "Heart Rate Alert",
                message = "Your heart rate was elevated during your workout",
                timestamp = "4 days ago",
                type = NotificationType.ALERT,
                isRead = true
            ),
            Notification(
                id = 8,
                title = "Prescription Renewal",
                message = "Your prescription for Amlodipine is due for renewal",
                timestamp = "5 days ago",
                type = NotificationType.RECORD,
                isRead = true
            ),
            Notification(
                id = 9,
                title = "Device Battery Low",
                message = "Xiaomi Mi Band battery is below 20%",
                timestamp = "6 days ago",
                type = NotificationType.DEVICE,
                isRead = true
            ),
            Notification(
                id = 10,
                title = "Appointment Reminder",
                message = "Don't forget your checkup appointment on Friday",
                timestamp = "1 week ago",
                type = NotificationType.APPOINTMENT,
                isRead = true
            )
        )
    }
    
    val unreadCount = notifications.count { !it.isRead }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Notifications")
                        if (unreadCount > 0) {
                            Text(
                                text = "$unreadCount unread",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Mark all as read", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.DoneAll, contentDescription = "Mark all read")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications) { notification ->
                NotificationItem(
                    notification = notification,
                    onClick = {
                        Toast.makeText(
                            context,
                            notification.title,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Surface else Primary.copy(alpha = 0.05f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getNotificationColor(notification.type).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = null,
                    tint = getNotificationColor(notification.type),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Primary)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.APPOINTMENT -> Icons.Filled.CalendarMonth
        NotificationType.CONSENT -> Icons.Filled.Security
        NotificationType.RECORD -> Icons.Filled.Description
        NotificationType.DEVICE -> Icons.Filled.Watch
        NotificationType.ALERT -> Icons.Filled.Warning
    }
}

fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.APPOINTMENT -> Primary
        NotificationType.CONSENT -> Warning
        NotificationType.RECORD -> Success
        NotificationType.DEVICE -> Secondary
        NotificationType.ALERT -> Error
    }
}
