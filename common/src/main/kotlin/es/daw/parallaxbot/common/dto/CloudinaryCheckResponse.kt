package es.daw.parallaxbot.common.dto

import kotlinx.serialization.Serializable

/**
 * Artifact lookup response returned by ms-cloudinary for a given event identifier.
 */
@Serializable
data class CloudinaryCheckResponse(
    val exists: Boolean,
    val url: String? = null,
    val eventId: String? = null
)