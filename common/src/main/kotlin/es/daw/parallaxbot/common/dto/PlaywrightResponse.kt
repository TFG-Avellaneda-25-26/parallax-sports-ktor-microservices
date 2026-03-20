package es.daw.parallaxbot.common.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlaywrightResponse(
    val url: String,
    val eventId: String,
    val isCached: Boolean
)