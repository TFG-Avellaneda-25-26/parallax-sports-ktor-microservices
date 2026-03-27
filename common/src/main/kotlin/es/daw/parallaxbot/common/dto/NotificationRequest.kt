package es.daw.parallaxbot.common.dto

import kotlinx.serialization.Serializable

/**
 * Generic notification request carrying event label and optional artifact URL.
 */
@Serializable
data class NotificationRequest(
    val eventName: String,
    val imageUrl: String
)