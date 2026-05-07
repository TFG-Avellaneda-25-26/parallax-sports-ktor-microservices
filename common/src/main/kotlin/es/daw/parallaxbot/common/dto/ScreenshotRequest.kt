package es.daw.parallaxbot.common.dto

import kotlinx.serialization.Serializable

@Serializable
data class ScreenshotRequest(
    val eventId: Long,
    val channel: String,
    val timezone: String? = null,
    val renderHash: String? = null,
)
