package es.daw.parallaxbot.playwright.dto

import kotlinx.serialization.Serializable

/**
 * Request payload for direct screenshot generation endpoints.
 */
@Serializable
data class ScreenshotRequest(
    val url: String,
    val eventId: String
)