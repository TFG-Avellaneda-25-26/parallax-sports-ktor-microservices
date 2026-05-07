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

class SpringCallbackService(
    private val httpClient: HttpClient,
) {
    private val baseUrl: String = "http://localhost:8080"
    private val logger = LoggerFactory.getLogger(this::class.java)

    // -> Source: Worker Delivery Result || Action: POST alert callback status to Spring API || Strategy: Return false on transport or non-success failures
    // -> API: /api/internal/alerts/{alertId}/status || Auth: internal network || Scope: worker status callback
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