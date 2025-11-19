package com.example.cloudcareapp.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility for formatting timestamps in IST (Indian Standard Time)
 * All backend timestamps are in UTC and need to be converted to IST (UTC+5:30)
 */
object TimeFormatter {
    
    // IST is UTC+05:30
    private val IST: ZoneId = ZoneId.of("Asia/Kolkata")
    
    // Formatters
    private val ISO_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
    private val DISPLAY_DATE_TIME: DateTimeFormatter = DateTimeFormatter
        .ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        .withZone(IST)
    
    private val DISPLAY_TIME: DateTimeFormatter = DateTimeFormatter
        .ofPattern("hh:mm a", Locale.ENGLISH)
        .withZone(IST)
    
    private val DISPLAY_DATE: DateTimeFormatter = DateTimeFormatter
        .ofPattern("dd MMM yyyy", Locale.ENGLISH)
        .withZone(IST)
    
    /**
     * Convert ISO 8601 UTC timestamp to formatted IST string
     * 
     * @param isoTimestamp ISO 8601 format (e.g., "2025-11-19T05:50:01.236" or "2025-11-19T05:50:01.236Z")
     * @return Formatted string in IST (e.g., "19 Nov 2025, 11:20 AM")
     */
    fun parseUtcToIst(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) {
            return "Never"
        }
        
        return try {
            // Parse ISO 8601 timestamp
            val cleanTimestamp = isoTimestamp.trim()
            val instant = if (cleanTimestamp.endsWith("Z")) {
                Instant.parse(cleanTimestamp)
            } else {
                // Handle timestamps without Z suffix
                Instant.parse("${cleanTimestamp}Z")
            }
            
            // Convert to IST and format
            DISPLAY_DATE_TIME.format(instant)
        } catch (e: Exception) {
            // Fallback: return original if parsing fails
            isoTimestamp
        }
    }
    
    /**
     * Get just the time in IST format
     * 
     * @param isoTimestamp ISO 8601 format
     * @return Time string (e.g., "11:20 AM")
     */
    fun parseUtcToIstTime(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) {
            return "--:-- AM"
        }
        
        return try {
            val cleanTimestamp = isoTimestamp.trim()
            val instant = if (cleanTimestamp.endsWith("Z")) {
                Instant.parse(cleanTimestamp)
            } else {
                Instant.parse("${cleanTimestamp}Z")
            }
            DISPLAY_TIME.format(instant)
        } catch (e: Exception) {
            "--:-- AM"
        }
    }
    
    /**
     * Get just the date in IST format
     * 
     * @param isoTimestamp ISO 8601 format
     * @return Date string (e.g., "19 Nov 2025")
     */
    fun parseUtcToIstDate(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) {
            return "No date"
        }
        
        return try {
            val cleanTimestamp = isoTimestamp.trim()
            val instant = if (cleanTimestamp.endsWith("Z")) {
                Instant.parse(cleanTimestamp)
            } else {
                Instant.parse("${cleanTimestamp}Z")
            }
            DISPLAY_DATE.format(instant)
        } catch (e: Exception) {
            "No date"
        }
    }
    
    /**
     * Get relative time format (e.g., "2 hours ago", "just now")
     * 
     * @param isoTimestamp ISO 8601 format
     * @return Relative time string
     */
    fun getRelativeTime(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) {
            return "Never"
        }
        
        return try {
            val cleanTimestamp = isoTimestamp.trim()
            val instant = if (cleanTimestamp.endsWith("Z")) {
                Instant.parse(cleanTimestamp)
            } else {
                Instant.parse("${cleanTimestamp}Z")
            }
            
            val now = Instant.now()
            val diff = now.toEpochMilli() - instant.toEpochMilli()
            
            when {
                diff < 60_000 -> "just now"
                diff < 3_600_000 -> "${diff / 60_000} min ago"
                diff < 86_400_000 -> "${diff / 3_600_000} hour${if (diff / 3_600_000 > 1) "s" else ""} ago"
                diff < 604_800_000 -> "${diff / 86_400_000} day${if (diff / 86_400_000 > 1) "s" else ""} ago"
                else -> "${diff / 604_800_000} week${if (diff / 604_800_000 > 1) "s" else ""} ago"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * Format LocalDateTime to IST display format
     * 
     * @param localDateTime LocalDateTime object
     * @return Formatted string in IST
     */
    fun formatLocalDateTimeToIst(localDateTime: LocalDateTime?): String {
        if (localDateTime == null) {
            return "Never"
        }
        
        return try {
            val zonedDateTime = localDateTime.atZone(IST)
            DISPLAY_DATE_TIME.format(zonedDateTime)
        } catch (e: Exception) {
            "Invalid date"
        }
    }
    
    /**
     * Parse last sync time and format for display
     * Shows time in IST with relative time
     * E.g., "Last synced: 19 Nov 2025, 11:20 AM (2 hours ago)"
     * 
     * @param isoTimestamp ISO 8601 format
     * @return Formatted last sync string
     */
    fun formatLastSyncTime(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) {
            return "Last synced: Never"
        }
        
        return try {
            val absoluteTime = parseUtcToIst(isoTimestamp)
            val relativeTime = getRelativeTime(isoTimestamp)
            "Last synced: $absoluteTime ($relativeTime)"
        } catch (e: Exception) {
            "Last synced: Unknown"
        }
    }
}
