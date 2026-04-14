package es.daw.parallaxbot.common.client

import es.daw.parallaxbot.common.dto.PlaywrightResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.slf4j.LoggerFactory

/**
 * Internal HTTP client used to request screenshot artifact generation from ms-playwright.
 */
class PlaywrightClient(
    private val httpClient: HttpClient,
) {
    val playwrightBaseUrl: String = "http://localhost:8082"

    private val logger = LoggerFactory.getLogger(PlaywrightClient::class.java)

    // -> Source: Worker Artifact Requirement || Action: Request screenshot generation from Playwright || Strategy: Return structured failure payload on network/API errors
    // -> API: /api/internal/screenshot || Auth: internal network || Scope: event screenshot artifact generation
    suspend fun generateEventScreenshot(eventId: Long): PlaywrightResponse {
        return try {
            val response = httpClient.post("$playwrightBaseUrl/api/internal/screenshot") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("eventId" to eventId))
            }

            if (response.status.isSuccess()) {
                response.body<PlaywrightResponse>()
            } else {
                PlaywrightResponse(false, errorMessage = "Playwright service returned ${response.status}")
            }
        } catch (e: Exception) {
            logger.error("Error calling Playwright service: ${e.message}")
            PlaywrightResponse(false, errorMessage = e.message)
        }
    }
}