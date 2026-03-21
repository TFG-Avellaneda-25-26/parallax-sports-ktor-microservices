package es.daw.parallaxbot.common.dto

/**
 * Canonical alert message consumed by worker channels from Redis streams.
 */
data class AlertStreamMessage(
    val schemaVersion: String,
    val alertId: Long,
    val userId: Long,
    val eventId: Long,
    val channel: String,
    val sendAtUtc: String,
    val idempotencyKey: String,
    val attempts: Int,
    val maxAttempts: Int,
    val artifactRequired: Boolean,
    val artifactId: Long? = null,
    // Optional
    val eventName: String? = null,
    val eventType: String? = null,
    val eventStatus: String? = null,
    val eventStartTimeUtc: String? = null,
    val eventEndTimeUtc: String? = null,
    val competitionName: String? = null,
    val venueName: String? = null,
    val venueTimezone: String? = null
)