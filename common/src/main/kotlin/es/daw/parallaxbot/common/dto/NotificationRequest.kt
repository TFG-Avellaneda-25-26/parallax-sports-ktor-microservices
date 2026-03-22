package es.daw.parallaxbot.common.dto

import kotlinx.serialization.Serializable

/**
 * Notification request payload used by provider-facing endpoints.
 */
@Serializable
data class NotificationRequest(
    val eventName: String,
    val imageUrl: String
)