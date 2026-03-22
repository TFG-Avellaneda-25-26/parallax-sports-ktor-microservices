package es.daw.parallaxbot.common.service

import es.daw.parallaxbot.common.dto.AlertStatusCallback
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.slf4j.LoggerFactory

/**
 * Sends delivery status callbacks to the Spring API after provider processing.
 */
class SpringCallbackService(
    private val httpClient: HttpClient,
) {
    private val baseUrl: String = "http://localhost:8080"
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Posts alert delivery status for one alert.
     *
     * @param alertId target alert identifier in the upstream API.
     * @param statusCallback final provider status payload.
     * @return true when callback endpoint accepts the status; false when transport or server fails.
     */
    suspend fun sendStatus(alertId: Long, statusCallback: AlertStatusCallback): Boolean {
        return try {
            val response = httpClient.post("$baseUrl/api/internal/alerts/$alertId/status") {
                contentType(ContentType.Application.Json)
                setBody(statusCallback)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            logger.error("Failed callback for alert $alertId: ${e.message}")
            false
        }
    }
}