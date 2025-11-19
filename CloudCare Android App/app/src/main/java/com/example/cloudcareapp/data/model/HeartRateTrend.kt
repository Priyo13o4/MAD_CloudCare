package com.example.cloudcareapp.data.model

/**
 * Heart rate trend data point with baseline-relative positioning.
 * 
 * Zero-point is 72 BPM (Apple Health's standard resting heart rate baseline).
 * Heart rate can be above or below 72, enabling suspended bar visualization
 * where bars extend both up and down from the center axis.
 * 
 * Data comes from the /metrics/heart-rate-trends backend endpoint which
 * aggregates daily average heart rate readings.
 */
data class HeartRateTrendDataPoint(
    val date: String,           // "2025-11-18"
    val bpm: Double,            // Average BPM for the day
    val min_bpm: Double,        // Minimum BPM for the day
    val max_bpm: Double         // Maximum BPM for the day
) {
    companion object {
        const val BASELINE_BPM = 72.0  // Zero-point for visualization
    }
    
    /**
     * Offset from baseline for suspended bar positioning.
     * Positive: above baseline (higher than 72 BPM)
     * Negative: below baseline (lower than 72 BPM)
     */
    val baselineOffset: Double
        get() = bpm - BASELINE_BPM
}

/**
 * Response wrapper for heart rate trends API endpoint.
 */
data class HeartRateTrendsResponse(
    val patient_id: String,
    val days: Int,
    val data: List<HeartRateTrendDataPoint>
)
