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
    val source_app: String?,
    val device_id: String?,
    val metadata: Map<String, Any>? = null
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
    val count: Int? = null
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
