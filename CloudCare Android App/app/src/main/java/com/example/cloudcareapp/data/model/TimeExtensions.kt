package com.example.cloudcareapp.data.model

import com.example.cloudcareapp.utils.TimeFormatter

/**
 * Extension functions for time-related models
 * Converts UTC timestamps to IST format for display
 */

/**
 * Get formatted last sync time in IST
 * Example output: "2 hours ago" or "19 Nov 2025, 11:20 AM"
 */
fun WearableDevice.getFormattedLastSyncTime(): String {
    return TimeFormatter.parseUtcToIst(this.last_sync_time)
}

/**
 * Get relative time for last sync (e.g., "2 hours ago")
 */
fun WearableDevice.getLastSyncRelativeTime(): String {
    return TimeFormatter.getRelativeTime(this.last_sync_time)
}

/**
 * Get just the date of last sync (e.g., "19 Nov 2025")
 */
fun WearableDevice.getLastSyncDate(): String {
    return TimeFormatter.parseUtcToIstDate(this.last_sync_time)
}

/**
 * Get just the time of last sync (e.g., "11:20 AM")
 */
fun WearableDevice.getLastSyncTime(): String {
    return TimeFormatter.parseUtcToIstTime(this.last_sync_time)
}

/**
 * Extension for HealthMetric timestamps
 */
fun HealthMetric.getFormattedTimestamp(): String {
    return TimeFormatter.parseUtcToIst(this.timestamp)
}

fun HealthMetric.getFormattedDate(): String {
    return TimeFormatter.parseUtcToIstDate(this.timestamp)
}

fun HealthMetric.getFormattedTime(): String {
    return TimeFormatter.parseUtcToIstTime(this.timestamp)
}

fun HealthMetric.getRelativeTimestamp(): String {
    return TimeFormatter.getRelativeTime(this.timestamp)
}

/**
 * Extension for Activity timestamps
 */
fun Activity.getFormattedTimestamp(): String {
    return TimeFormatter.parseUtcToIst(this.timestamp)
}

fun Activity.getRelativeTimestamp(): String {
    return TimeFormatter.getRelativeTime(this.timestamp)
}

/**
 * Extension for Consent timestamps
 */
fun Consent.getFormattedRequestTime(): String {
    return TimeFormatter.parseUtcToIst(this.timestamp)
}

fun Consent.getRelativeRequestTime(): String {
    return TimeFormatter.getRelativeTime(this.timestamp)
}

/**
 * Extension for EmergencyAlert timestamps (Doctor app)
 */
fun EmergencyAlert.getFormattedTimestamp(): String {
    return TimeFormatter.parseUtcToIst(this.timestamp)
}

fun EmergencyAlert.getRelativeTimestamp(): String {
    return TimeFormatter.getRelativeTime(this.timestamp)
}

/**
 * Extension for EmergencyCase timestamps (Hospital app)
 */
fun EmergencyCase.getFormattedAdmittedTime(): String {
    return TimeFormatter.parseUtcToIst(this.admittedTime)
}

fun EmergencyCase.getRelativeAdmittedTime(): String {
    return TimeFormatter.getRelativeTime(this.admittedTime)
}
