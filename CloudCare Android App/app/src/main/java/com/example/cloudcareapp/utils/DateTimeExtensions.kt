package com.example.cloudcareapp.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Converts a UTC datetime string from the backend to local timezone,
 * then formats it for display.
 * 
 * Input formats:
 * - "2025-11-19" (date only)
 * - "2025-11-19 05:00" or "2025-11-19T05:00" (datetime)
 * - "2025-11-19 05:00:00" or "2025-11-19T05:00:00Z" (full datetime)
 * 
 * Output: Formatted string in local timezone
 * - "HH:mm" for hourly data (e.g., "10:30")
 * - "MMM dd" for daily data (e.g., "Nov 19")
 */
fun String.formatDateWithTimezone(outputFormat: String = "HH:mm"): String {
    return try {
        val inputStr = this.trim()
            .replace("T", " ")
            .replace("Z", "")
            .trim()
        
        // Try to parse with different formats
        val utcDate = when {
            // Date only (e.g., "2025-11-19")
            inputStr.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")) -> {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                dateFormat.parse(inputStr)
            }
            // Date with time (e.g., "2025-11-19 05:00" or "2025-11-19 05:00:00")
            inputStr.contains(" ") -> {
                val hasSeconds = inputStr.split(":").size > 2
                val dateFormat = SimpleDateFormat(
                    if (hasSeconds) "yyyy-MM-dd HH:mm:ss" else "yyyy-MM-dd HH:mm",
                    Locale.US
                )
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                dateFormat.parse(inputStr)
            }
            else -> return this
        }
        
        if (utcDate == null) return this
        
        // Format to IST (Asia/Kolkata) timezone
        val localFormat = SimpleDateFormat(outputFormat, Locale.US)
        localFormat.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        
        localFormat.format(utcDate)
    } catch (e: Exception) {
        // If parsing fails, return original string
        this
    }
}

/**
 * Convert UTC datetime string to local timezone and return as Calendar for advanced operations
 */
fun String.toLocalCalendar(): Calendar {
    return try {
        val inputStr = this.trim()
            .replace("T", " ")
            .replace("Z", "")
            .trim()
        
        // Try to parse with different formats
        val utcDate = when {
            // Date only
            inputStr.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")) -> {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                dateFormat.parse(inputStr)
            }
            // Date with time
            inputStr.contains(" ") -> {
                val hasSeconds = inputStr.split(":").size > 2
                val dateFormat = SimpleDateFormat(
                    if (hasSeconds) "yyyy-MM-dd HH:mm:ss" else "yyyy-MM-dd HH:mm",
                    Locale.US
                )
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                dateFormat.parse(inputStr)
            }
            else -> return Calendar.getInstance()
        }
        
        if (utcDate == null) return Calendar.getInstance()
        
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        calendar.time = utcDate
        return calendar
    } catch (e: Exception) {
        Calendar.getInstance()
    }
}

/**
 * Get the hour from a UTC datetime string (converted to local timezone)
 */
fun String.getHourInLocalTimezone(): Int {
    return try {
        val calendar = this.toLocalCalendar()
        calendar.get(Calendar.HOUR_OF_DAY)
    } catch (e: Exception) {
        0
    }
}

/**
 * Check if two UTC datetime strings represent the same day in local timezone
 */
fun String.isSameDayInLocalTimezone(other: String): Boolean {
    return try {
        val cal1 = this.toLocalCalendar()
        val cal2 = other.toLocalCalendar()
        
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    } catch (e: Exception) {
        false
    }
}
