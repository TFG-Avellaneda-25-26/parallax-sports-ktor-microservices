package es.daw.parallaxbot.common.dto

import kotlinx.serialization.Serializable

/**
 * Response contract returned by screenshot orchestration endpoint.
 */
@Serializable
data class PlaywrightResponse(
    val success: Boolean,
    val url: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null
)