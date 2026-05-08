package es.daw.parallaxbot.common.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationRequest(
    val eventName: String,
    val imageUrl: String
)