package es.daw.parallaxbot.playwright.dto

import kotlinx.serialization.Serializable

@Serializable
data class ScreenshotRequest(
    val url: String,
    val eventId: String
)