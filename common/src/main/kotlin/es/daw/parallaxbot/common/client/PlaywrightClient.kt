package es.daw.parallaxbot.common.client

import es.daw.parallaxbot.common.dto.PlaywrightResponse
import es.daw.parallaxbot.common.dto.ScreenshotRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.server.config.ApplicationConfig
import org.slf4j.LoggerFactory

/**
 * Internal HTTP client used to request screenshot artifact generation from ms-playwright.
 */
class PlaywrightClient(
    private val httpClient: HttpClient,
    config: ApplicationConfig,
) {
    private val playwrightBaseUrl: String = config.property("parallaxbot.playwright.base-url").getString()

    private val logger = LoggerFactory.getLogger(PlaywrightClient::class.java)

    suspend fun generateEventScreenshot(
        eventId: Long,
        channel: String,
        timezone: String?,
        renderHash: String?,
    ): PlaywrightResponse {
        return try {
            val response = httpClient.post("$playwrightBaseUrl/api/internal/screenshot") {
                contentType(ContentType.Application.Json)
                setBody(ScreenshotRequest(eventId, channel, timezone, renderHash))
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
