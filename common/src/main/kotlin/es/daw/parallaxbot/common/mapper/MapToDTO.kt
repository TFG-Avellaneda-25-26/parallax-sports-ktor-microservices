package es.daw.parallaxbot.common.mapper

import es.daw.parallaxbot.common.dto.AlertStreamMessage

/**
 * Maps raw Redis stream fields into the normalized alert delivery contract.
 */
fun mapToDto(p: Map<String, String>): AlertStreamMessage {
    return AlertStreamMessage(
        schemaVersion = p["schemaVersion"] ?: "v1",
        alertId = p["alertId"]?.toLong() ?: 0L,
        userId = p["userId"]?.toLong() ?: 0L,
        eventId = p["eventId"]?.toLong() ?: 0L,
        channel = p["channel"] ?: "unknown",
        sendAtUtc = p["sendAtUtc"] ?: "",
        idempotencyKey = p["idempotencyKey"] ?: "",
        attempts = p["attempts"]?.toInt() ?: 0,
        maxAttempts = p["maxAttempts"]?.toInt() ?: 6,
        artifactRequired = p["artifactRequired"]?.toBoolean() ?: false,
        artifactId = p["artifactId"]?.toLong(),
        eventName = p["eventName"],
        eventType = p["eventType"],
        eventStatus = p["eventStatus"],
        eventStartTimeUtc = p["eventStartTimeUtc"],
        eventEndTimeUtc = p["eventEndTimeUtc"],
        competitionName = p["competitionName"],
        venueName = p["venueName"],
        venueTimezone = p["venueTimezone"]
    )
}