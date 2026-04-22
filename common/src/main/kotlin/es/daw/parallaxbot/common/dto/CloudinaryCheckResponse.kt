package es.daw.parallaxbot.common.dto

import kotlinx.serialization.Serializable

@Serializable
data class CloudinaryCheckResponse(
    val exists: Boolean,
    val url: String? = null,
    val eventId: String? = null
)