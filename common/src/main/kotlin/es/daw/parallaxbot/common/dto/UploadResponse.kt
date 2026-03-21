package es.daw.parallaxbot.common.dto

import kotlinx.serialization.Serializable

/**
 * Response payload for image upload operations.
 */
@Serializable
data class UploadResponse(
    val success: Boolean,
    val url: String? = null,
    val publicId: String? = null,
    val eventId: String? = null,
    val errorMessage: String? = null
)