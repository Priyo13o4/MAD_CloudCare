package com.example.cloudcareapp.data.model

/**
 * Sleep trend data point for accurate daily sleep visualization.
 * 
 * This model separates "Time in Bed" from "Time Asleep" to provide
 * honest sleep efficiency metrics without hardcoded percentages.
 * 
 * Data comes from the /metrics/sleep-trends backend endpoint which
 * correctly aggregates Apple Health sleep categories:
 * - time_in_bed: From 'inBed' samples
 * - time_asleep: Sum of 'core' + 'deep' + 'rem' samples (excludes 'awake')
 */
data class SleepTrendDataPoint(
    val date: String,           // "2025-11-18"
    val time_in_bed: Double,    // Total hours in bed
    val time_asleep: Double     // Actual sleep hours (core + deep + rem)
) {
    /**
     * Calculate sleep efficiency as a percentage.
     * Returns 0 if time_in_bed is 0 to avoid division by zero.
     */
    val sleepEfficiency: Double
        get() = if (time_in_bed > 0) (time_asleep / time_in_bed) * 100 else 0.0
}

/**
 * Response wrapper for sleep trends API endpoint.
 */
data class SleepTrendsResponse(
    val patient_id: String,
    val days: Int,
    val data: List<SleepTrendDataPoint>
)
