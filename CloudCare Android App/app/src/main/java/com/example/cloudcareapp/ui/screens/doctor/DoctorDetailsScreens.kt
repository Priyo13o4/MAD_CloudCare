package com.example.cloudcareapp.ui.screens.doctor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.cloudcareapp.data.MockDoctorData
import com.example.cloudcareapp.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorPatientsScreen(
    onBackClick: () -> Unit = {}
) {
    val patients = remember { MockDoctorData.MOCK_ASSIGNED_PATIENTS }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Patients (${patients.size})") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Secondary
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
            // Critical patients section
            val criticalPatients = patients.filter { it.status == PatientStatus.CRITICAL || it.emergencyFlag }
            if (criticalPatients.isNotEmpty()) {
                item {
                    Text(
                        text = "⚠️ Critical Patients",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(criticalPatients) { patient ->
                    PatientCard(patient = patient, showEmergencyBanner = true)
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "All Patients",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            
            // All other patients
            items(patients.filter { it.status != PatientStatus.CRITICAL && !it.emergencyFlag }) { patient ->
                PatientCard(patient = patient)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientCard(
    patient: AssignedPatient,
    showEmergencyBanner: Boolean = false
) {
    val statusColor = when (patient.status) {
        PatientStatus.STABLE -> Success
        PatientStatus.MONITORING -> Warning
        PatientStatus.CRITICAL -> Error
    }
    
    val statusIcon = when (patient.status) {
        PatientStatus.STABLE -> Icons.Filled.CheckCircle
        PatientStatus.MONITORING -> Icons.Filled.Warning
        PatientStatus.CRITICAL -> Icons.Filled.Error
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (showEmergencyBanner) Error.copy(alpha = 0.05f) else Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (showEmergencyBanner) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Emergency banner
            if (showEmergencyBanner) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Error)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "EMERGENCY - Immediate Attention Required",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Patient info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Secondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = patient.name.split(" ").map { it.first() }.joinToString(""),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Secondary
                    )
                }
                
                // Patient details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = patient.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "(${patient.age}, ${patient.gender})",
                            style = MaterialTheme.typography.bodyMedium,
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
                            text = "Status: ${patient.status.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Text(
                        text = "Condition: ${patient.condition}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Next Appointment",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = patient.nextAppointment,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (patient.nextAppointment == "Emergency") Error else TextPrimary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Last Visit",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = patient.lastVisit,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Action buttons
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO: View details */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Secondary
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
                    onClick = { /* TODO: Call patient */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorEmergencyScreen(
    onBackClick: () -> Unit = {}
) {
    val alerts = remember { MockDoctorData.MOCK_EMERGENCY_ALERTS }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Alerts (${alerts.size})") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Secondary
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
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = Error
                        )
                        Text(
                            text = "Critical alerts require immediate attention",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
            
            items(alerts) { alert ->
                EmergencyAlertCard(alert = alert)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyAlertCard(alert: EmergencyAlert) {
    val severityColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> Error
        AlertSeverity.HIGH -> Color(0xFFFF6B00) // Orange
        AlertSeverity.MEDIUM -> Warning
        AlertSeverity.LOW -> Success
    }
    
    val alertIcon = when (alert.alertType) {
        AlertType.HEART_RATE -> Icons.Filled.Favorite
        AlertType.OXYGEN_LEVEL -> Icons.Filled.Air
        AlertType.BLOOD_PRESSURE -> Icons.Filled.HealthAndSafety
        AlertType.TEMPERATURE -> Icons.Filled.Thermostat
        AlertType.OTHER -> Icons.Filled.Warning
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (alert.severity == AlertSeverity.CRITICAL) 4.dp else 2.dp
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
                            text = "${alert.severity.name} ALERT",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = alert.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            // Alert details
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
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(severityColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = alertIcon,
                            contentDescription = null,
                            tint = severityColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = alert.patientName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Patient ID: ${alert.patientId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                
                Divider()
                
                // Alert message
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Alert Type: ${alert.alertType.name.replace("_", " ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = alert.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    if (alert.currentValue.isNotEmpty()) {
                        Text(
                            text = "Current Value: ${alert.currentValue}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = severityColor
                        )
                    }
                }
                
                Divider()
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* TODO: View patient */ },
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
                        Text("View Patient")
                    }
                    OutlinedButton(
                        onClick = { /* TODO: Dismiss */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorScheduleScreen(
    onBackClick: () -> Unit = {}
) {
    val todaysAppointments = remember { MockDoctorData.getTodaysAppointments() }
    val allAppointments = remember { MockDoctorData.MOCK_DOCTOR_APPOINTMENTS }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Schedule") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Secondary
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Today's date card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Secondary
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "Today - November 9, 2025",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${todaysAppointments.size} appointments scheduled",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
            
            // Today's appointments section
            if (todaysAppointments.isNotEmpty()) {
                item {
                    Text(
                        text = "Today's Appointments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(todaysAppointments) { appointment ->
                    AppointmentCard(appointment = appointment)
                }
            }
            
            // Recent/Past appointments section
            val pastAppointments = allAppointments.filter { it.status == AppointmentStatus.COMPLETED }
            if (pastAppointments.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Recent Appointments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(pastAppointments) { appointment ->
                    AppointmentCard(appointment = appointment)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentCard(appointment: DoctorAppointment) {
    val statusColor = when (appointment.status) {
        AppointmentStatus.SCHEDULED -> Primary
        AppointmentStatus.COMPLETED -> Success
        AppointmentStatus.CANCELLED -> TextSecondary
        AppointmentStatus.IN_PROGRESS -> Warning
    }
    
    val statusIcon = when (appointment.status) {
        AppointmentStatus.SCHEDULED -> Icons.Filled.Schedule
        AppointmentStatus.COMPLETED -> Icons.Filled.CheckCircle
        AppointmentStatus.CANCELLED -> Icons.Filled.Cancel
        AppointmentStatus.IN_PROGRESS -> Icons.Filled.PlayArrow
    }
    
    val isEmergency = appointment.type == "Emergency"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEmergency) Error.copy(alpha = 0.05f) else Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isEmergency) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Emergency banner
            if (isEmergency) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Error)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Emergency,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "EMERGENCY APPOINTMENT",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Appointment details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Time and patient
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = appointment.time,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isEmergency) Error else TextPrimary
                            )
                            Text(
                                text = appointment.date,
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
                                text = appointment.status.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                Divider()
                
                // Patient and reason
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                            text = appointment.patientName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "(${appointment.patientId})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MedicalServices,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Type: ${appointment.type}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Reason: ${appointment.reason}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Department: ${appointment.department}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    
                    if (appointment.notes.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notes,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = appointment.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
                
                // Action buttons (only for scheduled/in-progress)
                if (appointment.status == AppointmentStatus.SCHEDULED || appointment.status == AppointmentStatus.IN_PROGRESS) {
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { /* TODO: Start appointment */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEmergency) Error else Secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (appointment.status == AppointmentStatus.IN_PROGRESS) "Continue" else "Start")
                        }
                        OutlinedButton(
                            onClick = { /* TODO: Reschedule */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reschedule")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorRecordsScreen(
    onBackClick: () -> Unit = {}
) {
    val records = remember { MockDoctorData.MOCK_PATIENT_RECORDS }
    var selectedRecordType by remember { mutableStateOf<DoctorRecordType?>(null) }
    
    val filteredRecords = if (selectedRecordType != null) {
        records.filter { it.recordType == selectedRecordType }
    } else {
        records
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Records (${records.size})") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Secondary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
        ) {
            // Filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedRecordType == null,
                        onClick = { selectedRecordType = null },
                        label = { Text("All (${records.size})") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                DoctorRecordType.values().forEach { type ->
                    item {
                        val count = records.count { it.recordType == type }
                        FilterChip(
                            selected = selectedRecordType == type,
                            onClick = { selectedRecordType = type },
                            label = { Text("${type.name.replace("_", " ")} ($count)") }
                        )
                    }
                }
            }
            
            // Records list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRecords) { record ->
                    PatientRecordCard(record = record)
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientRecordCard(record: PatientRecord) {
    val recordTypeColor = when (record.recordType) {
        DoctorRecordType.CHECKUP -> Primary
        DoctorRecordType.LAB_RESULT -> Secondary
        DoctorRecordType.PRESCRIPTION -> Success
        DoctorRecordType.DIAGNOSIS -> Warning
        DoctorRecordType.SURGERY -> Error
        DoctorRecordType.EMERGENCY -> Error
    }
    
    val recordTypeIcon = when (record.recordType) {
        DoctorRecordType.CHECKUP -> Icons.Filled.HealthAndSafety
        DoctorRecordType.LAB_RESULT -> Icons.Filled.Science
        DoctorRecordType.PRESCRIPTION -> Icons.Filled.Medication
        DoctorRecordType.DIAGNOSIS -> Icons.Filled.Assignment
        DoctorRecordType.SURGERY -> Icons.Filled.MedicalServices
        DoctorRecordType.EMERGENCY -> Icons.Filled.Emergency
    }
    
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Record type banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(recordTypeColor.copy(alpha = 0.15f))
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
                            imageVector = recordTypeIcon,
                            contentDescription = null,
                            tint = recordTypeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = record.recordType.name.replace("_", " "),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = recordTypeColor
                        )
                    }
                    Text(
                        text = record.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            
            // Record details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Patient and title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(recordTypeColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = record.patientName.split(" ").map { it.first() }.joinToString(""),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = recordTypeColor
                        )
                    }
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = record.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = record.patientName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "• ${record.patientId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = TextSecondary
                    )
                }
                
                // Description
                Text(
                    text = record.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                // Expanded details
                if (expanded) {
                    Divider()
                    
                    if (record.diagnosis.isNotEmpty()) {
                        RecordDetailSection(
                            title = "Diagnosis",
                            content = record.diagnosis,
                            icon = Icons.Filled.Assignment,
                            color = Error
                        )
                    }
                    
                    if (record.prescriptions.isNotEmpty()) {
                        RecordDetailSection(
                            title = "Prescriptions",
                            content = record.prescriptions,
                            icon = Icons.Filled.Medication,
                            color = Success
                        )
                    }
                    
                    if (record.testResults.isNotEmpty()) {
                        RecordDetailSection(
                            title = "Test Results",
                            content = record.testResults,
                            icon = Icons.Filled.Science,
                            color = Secondary
                        )
                    }
                    
                    if (record.doctorNotes.isNotEmpty()) {
                        RecordDetailSection(
                            title = "Doctor's Notes",
                            content = record.doctorNotes,
                            icon = Icons.Filled.Notes,
                            color = Primary
                        )
                    }
                    
                    Divider()
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* TODO: Edit record */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }
                        OutlinedButton(
                            onClick = { /* TODO: Share record */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordDetailSection(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
