package com.example.cloudcareapp.ui.screens.hospital

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cloudcareapp.ui.theme.*
import com.example.cloudcareapp.data.MockHospitalData
import com.example.cloudcareapp.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalStaffScreen(
    onBackClick: () -> Unit = {}
) {
    val staff = remember { MockHospitalData.MOCK_HOSPITAL_STAFF }
    val activeStaff = staff.filter { it.status == StaffStatus.ACTIVE }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Staff (${staff.size})") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Success
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Success
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StaffStatItem(
                            label = "Total Staff",
                            value = "${staff.size}",
                            icon = Icons.Filled.People
                        )
                        StaffStatItem(
                            label = "Active",
                            value = "${activeStaff.size}",
                            icon = Icons.Filled.CheckCircle
                        )
                        StaffStatItem(
                            label = "On Leave",
                            value = "${staff.count { it.status == StaffStatus.ON_LEAVE }}",
                            icon = Icons.Filled.EventBusy
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "All Staff Members",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(staff) { staffMember ->
                StaffMemberCard(staff = staffMember)
            }
        }
    }
}

@Composable
fun StaffStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffMemberCard(staff: HospitalStaff) {
    val statusColor = when (staff.status) {
        StaffStatus.ACTIVE -> Success
        StaffStatus.ON_LEAVE -> Warning
        StaffStatus.UNAVAILABLE -> TextSecondary
    }
    
    val statusIcon = when (staff.status) {
        StaffStatus.ACTIVE -> Icons.Filled.CheckCircle
        StaffStatus.ON_LEAVE -> Icons.Filled.EventBusy
        StaffStatus.UNAVAILABLE -> Icons.Filled.Cancel
    }
    
    val isDoctorRole = staff.name.startsWith("Dr.")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDoctorRole) Icons.Filled.MedicalServices else Icons.Filled.LocalHospital,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Staff details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = staff.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "(${staff.age})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = staff.status.name.replace("_", " "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Text(
                        text = "Specialization: ${staff.specialization}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    
                    Text(
                        text = "Department: ${staff.department}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    
                    if (staff.patientCount > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.People,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${staff.patientCount} patients assigned",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                        }
                    }
                    
                    Text(
                        text = "Joined: ${staff.joinDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            
            Divider()
            
            // Contact info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = staff.email,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = staff.phone,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            
            // Action buttons
            if (staff.status == StaffStatus.ACTIVE) {
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* TODO: View profile */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Success
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Profile")
                    }
                    OutlinedButton(
                        onClick = { /* TODO: Message */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Message,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Message")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalResourcesScreen(
    onBackClick: () -> Unit = {}
) {
    val resources = remember { MockHospitalData.MOCK_HOSPITAL_RESOURCES }
    val criticalResources = resources.filter { it.status == ResourceStatus.CRITICAL }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resources Management") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Success
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Critical resources alert
            if (criticalResources.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Error.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = Error
                            )
                            Text(
                                text = "${criticalResources.size} resources at critical levels!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Error
                            )
                        }
                    }
                }
            }
            
            // Resource categories
            item {
                Text(
                    text = "Equipment & Supplies",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(resources) { resource ->
                ResourceCard(resource = resource)
            }
        }
    }
}

@Composable
fun ResourceCard(resource: HospitalResource) {
    val statusColor = when (resource.status) {
        ResourceStatus.NORMAL -> Success
        ResourceStatus.LOW -> Warning
        ResourceStatus.CRITICAL -> Error
    }
    
    val utilizationPercent = ((resource.inUse.toFloat() / resource.total) * 100).toInt()
    
    val categoryIcon = when (resource.category) {
        ResourceCategory.BEDS -> Icons.Filled.Bed
        ResourceCategory.EQUIPMENT -> Icons.Filled.MedicalServices
        ResourceCategory.SUPPLIES -> Icons.Filled.Inventory
        ResourceCategory.MEDICATION -> Icons.Filled.Medication
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (resource.status == ResourceStatus.CRITICAL) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Status banner for critical/low resources
            if (resource.status != ResourceStatus.NORMAL) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(statusColor.copy(alpha = 0.9f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${resource.status.name} LEVEL",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        if (resource.status == ResourceStatus.CRITICAL) {
                            Text(
                                text = "REORDER NOW",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            // Resource details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = resource.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = resource.category.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    // Status icon
                    Icon(
                        imageVector = when (resource.status) {
                            ResourceStatus.NORMAL -> Icons.Filled.CheckCircle
                            ResourceStatus.LOW -> Icons.Filled.Warning
                            ResourceStatus.CRITICAL -> Icons.Filled.Error
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Divider()
                
                // Resource statistics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ResourceStatColumn(
                        label = "Total",
                        value = "${resource.total}",
                        color = TextPrimary
                    )
                    ResourceStatColumn(
                        label = "Available",
                        value = "${resource.available}",
                        color = Success
                    )
                    ResourceStatColumn(
                        label = "In Use",
                        value = "${resource.inUse}",
                        color = Secondary
                    )
                    ResourceStatColumn(
                        label = "Utilization",
                        value = "$utilizationPercent%",
                        color = statusColor
                    )
                }
                
                // Progress bar
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Capacity Usage",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "${resource.inUse}/${resource.total}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                    LinearProgressIndicator(
                        progress = resource.inUse.toFloat() / resource.total,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = statusColor,
                        trackColor = statusColor.copy(alpha = 0.2f)
                    )
                }
                
                // Action button for critical resources
                if (resource.status == ResourceStatus.CRITICAL) {
                    Button(
                        onClick = { /* TODO: Reorder */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reorder ${resource.name}")
                    }
                }
            }
        }
    }
}

@Composable
fun ResourceStatColumn(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalAdmissionsScreen(
    onBackClick: () -> Unit = {}
) {
    val emergencyCases = remember { MockHospitalData.MOCK_EMERGENCY_CASES }
    val activeEmergencies = emergencyCases.filter { it.status != EmergencyStatus.DISCHARGED }
    val criticalCases = emergencyCases.filter { it.severity == EmergencySeverity.CRITICAL }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Admissions") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Success
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Success
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        EmergencyStatItem(
                            label = "Total Cases",
                            value = "${emergencyCases.size}",
                            icon = Icons.Filled.LocalHospital
                        )
                        EmergencyStatItem(
                            label = "Active",
                            value = "${activeEmergencies.size}",
                            icon = Icons.Filled.Emergency
                        )
                        EmergencyStatItem(
                            label = "Critical",
                            value = "${criticalCases.size}",
                            icon = Icons.Filled.Warning
                        )
                    }
                }
            }
            
            // Critical cases section
            if (criticalCases.isNotEmpty()) {
                item {
                    Text(
                        text = "🚨 Critical Cases",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(criticalCases.filter { it.status != EmergencyStatus.DISCHARGED }) { case ->
                    EmergencyCaseCard(emergencyCase = case, highlightCritical = true)
                }
            }
            
            // All emergency cases section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All Emergency Cases",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(emergencyCases) { case ->
                EmergencyCaseCard(emergencyCase = case)
            }
        }
    }
}

@Composable
fun EmergencyStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyCaseCard(
    emergencyCase: EmergencyCase,
    highlightCritical: Boolean = false
) {
    val severityColor = when (emergencyCase.severity) {
        EmergencySeverity.CRITICAL -> Error
        EmergencySeverity.HIGH -> Color(0xFFFF6B00) // Orange
        EmergencySeverity.MEDIUM -> Warning
        EmergencySeverity.LOW -> Success
    }
    
    val statusColor = when (emergencyCase.status) {
        EmergencyStatus.IN_TREATMENT -> Error
        EmergencyStatus.STABLE -> Success
        EmergencyStatus.WAITING -> Warning
        EmergencyStatus.DISCHARGED -> TextSecondary
    }
    
    val statusIcon = when (emergencyCase.status) {
        EmergencyStatus.IN_TREATMENT -> Icons.Filled.MedicalServices
        EmergencyStatus.STABLE -> Icons.Filled.CheckCircle
        EmergencyStatus.WAITING -> Icons.Filled.HourglassEmpty
        EmergencyStatus.DISCHARGED -> Icons.Filled.ExitToApp
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlightCritical) Error.copy(alpha = 0.05f) else Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (emergencyCase.severity == EmergencySeverity.CRITICAL) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Severity banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(severityColor)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
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
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${emergencyCase.severity.name} SEVERITY",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = emergencyCase.admittedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            // Patient details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Patient info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(severityColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emergencyCase.patientName.split(" ").map { it.first() }.joinToString(""),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = severityColor
                        )
                    }
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = emergencyCase.patientName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ID: ${emergencyCase.patientId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "• ${emergencyCase.age}${emergencyCase.gender}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    
                    // Status chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = emergencyCase.status.name.replace("_", " "),
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                Divider()
                
                // Condition and department
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.HealthAndSafety,
                            contentDescription = null,
                            tint = Error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Condition: ${emergencyCase.condition}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalHospital,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Department: ${emergencyCase.department}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Assigned: ${emergencyCase.assignedDoctor}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
                
                // Action buttons (only for active cases)
                if (emergencyCase.status != EmergencyStatus.DISCHARGED) {
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { /* TODO: View details */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = severityColor
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Details")
                        }
                        OutlinedButton(
                            onClick = { /* TODO: Update status */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Success
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Update,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Update")
                        }
                    }
                }
            }
        }
    }
}
