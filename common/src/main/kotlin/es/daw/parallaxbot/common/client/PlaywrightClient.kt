package es.daw.parallaxbot.common.client

import es.daw.parallaxbot.common.dto.PlaywrightResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import org.slf4j.LoggerFactory

/**
 * Calls the Playwright microservice to generate or fetch event screenshot artifacts.
 */
class PlaywrightClient(
    private val httpClient: HttpClient,
) {
    val playwrightBaseUrl: String = "http://localhost:8082"

    private val logger = LoggerFactory.getLogger(PlaywrightClient::class.java)

    /**
     * Requests a screenshot artifact for the provided event.
     *
     * @param eventId event identifier used by downstream services to resolve the source event.
     * @return response contract with success flag, URL when available, and error context otherwise.
     */
    suspend fun generateEventScreenshot(eventId: Long): PlaywrightResponse {
        return try {
            val response = httpClient.post("$playwrightBaseUrl/api/internal/screenshot") {
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