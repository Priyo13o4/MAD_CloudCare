package com.example.cloudcareapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Individual health metric data point from MongoDB
 */
data class HealthMetric(
    val metric_type: String,
    val value: Double,
    val unit: String,
    val timestamp: String,
    val start_date: String?,
    val end_date: String?,
    val source_app: String?,  // Device name from iOS (e.g., "Priyodip's Apple Watch")
    val device_id: String?,
    @Deprecated("Removed for data normalization. Device info now in PostgreSQL wearable_devices table")
    val metadata: Map<String, Any>? = null  // Deprecated - contains redundant data
)

/**
 * Summary statistics for a single metric
 */
data class MetricSummary(
    val total: Double? = null,
    val avg: Double? = null,
    val min: Double? = null,
    val max: Double? = null,
    val change: String? = null,
    val unit: String? = null,
    val count: Int? = null,
    // For sleep data: complete sleep information
    val time_in_bed: Double? = null,
    val time_asleep: Double? = null,
    val stages: SleepStages? = null,  // Overall stage breakdown
    val sessions: List<SleepSession>? = null  // Individual sleep sessions
)

/**
 * Sleep stage breakdown for detailed sleep analysis
 */
data class SleepBreakdown(
    val deep_hours: Double? = null,
    val core_hours: Double? = null,
    val rem_hours: Double? = null,
    val light_hours: Double? = null
)

/**
 * Sleep session with start/end times and stage breakdown
 */
data class SleepSession(
    val start_time: String,
    val end_time: String,
    val in_bed_hours: Double? = null,
    val asleep_hours: Double? = null,
    val stages: SleepStages? = null
)

/**
 * Sleep stage breakdown (in hours)
 */
data class SleepStages(
    val awake: Double = 0.0,
    val rem: Double = 0.0,
    val core: Double = 0.0,
    val deep: Double = 0.0
)

/**
 * Today's aggregated health summary
 */
data class TodaySummary(
    val steps: MetricSummary,
    val heart_rate: MetricSummary,
    val calories: MetricSummary,
    val distance: MetricSummary?,
    val flights_climbed: MetricSummary?,
    val sleep: MetricSummary?,
    val sleep_breakdown: SleepBreakdown? = null,
    val resting_heart_rate: MetricSummary? = null,
    val vo2_max: MetricSummary? = null
)

/**
 * Recent metrics response wrapper
 */
data class RecentMetricsResponse(
    val patient_id: String,
    val hours: Int,
    val count: Int,
    val metrics: List<HealthMetric>
)

/**
 * Today's summary response wrapper
 */
data class TodaySummaryResponse(
    val patient_id: String,
    val date: String,
    val summary: TodaySummary
)

/**
 * Aggregated metrics response wrapper
 */
data class AggregatedMetricsResponse(
    val patient_id: String,
    val period: String,
    val days: Int,
    val metrics: Map<String, List<AggregatedDataPoint>>
)

/**
 * Single aggregated data point
 */
data class AggregatedDataPoint(
    val date: String,
    val total: Double,
    val avg: Double,
    val min: Double,
    val max: Double,
    val count: Int
)

/**
 * Metrics by type response wrapper
 */
data class MetricsByTypeResponse(
    val patient_id: String,
    val metric_type: String,
    val start_date: String?,
    val end_date: String?,
    val count: Int,
    val metrics: List<HealthMetric>
)
