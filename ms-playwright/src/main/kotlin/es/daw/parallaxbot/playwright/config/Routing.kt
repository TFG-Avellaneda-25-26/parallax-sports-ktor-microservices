package es.daw.parallaxbot.playwright.config

import es.daw.parallaxbot.common.dto.EventDTO
import es.daw.parallaxbot.common.dto.CloudinaryCheckResponse
import es.daw.parallaxbot.common.dto.PlaywrightResponse
import es.daw.parallaxbot.common.dto.UploadResponse
import es.daw.parallaxbot.playwright.service.PlaywrightService
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

/**
 * Registers Playwright internal routes used to generate or resolve event screenshot artifacts.
 */
// -> Triggers: Ktor route registration at service startup || Contract: exposes screenshot orchestration endpoint
fun Application.configureRouting() {
    val playwrightService by inject<PlaywrightService>()
    val httpClient by inject<HttpClient>()

    val logger = LoggerFactory.getLogger("Routing")

    routing {
                /*============================================================
                    SCREENSHOT ORCHESTRATION
                    Validate request, resolve event data, generate image, upload artifact
                ============================================================*/
        // -> Triggers: POST /api/internal/screenshot || Contract: returns PlaywrightResponse (200/400/404/500)
        post("/api/internal/screenshot") {
            try {
                val request = call.receive<Map<String, Long>>()
                val eventId = request["eventId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing eventId")

                logger.info("Requesting screenshot for eventId: $eventId")

                val check = httpClient.get("http://localhost:8085/check/$eventId").body<CloudinaryCheckResponse>()

                if (check.exists && check.url != null) {
                    return@post call.respond(
                        HttpStatusCode.OK,
                        PlaywrightResponse(success = true, url = check.url)
                    )
                }

                val eventResponse = httpClient.get("http://localhost:8080/api/events/$eventId")

                if (!eventResponse.status.isSuccess()) {
                    return@post call.respond(HttpStatusCode.NotFound, PlaywrightResponse(success = false, errorMessage = "Could not find event with id: $eventId"))
                }

                val eventDto = httpClient.get("http://localhost:8080/api/internal/events/$eventId").body<EventDTO>()
                val imageBytes = playwrightService.generateEventImage(eventDto)

                if (imageBytes.isEmpty()) {
                    return@post call.respond(HttpStatusCode.InternalServerError,
                        PlaywrightResponse(success = false, errorMessage = "Could not find image with id: $eventId"))
                }

                val uploadResponse = httpClient.post("http://localhost:8085/upload") {
                    contentType(ContentType.MultiPart.FormData)
                    setBody(MultiPartFormDataContent(formData {
                        append("file", imageBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"$eventId.jpeg\"")
                        })
                        append("eventId", eventId)
                    }))
                }

                if (!uploadResponse.status.isSuccess()) {
                    return@post call.respond(
                        HttpStatusCode.InternalServerError,
                        PlaywrightResponse(success = false, errorMessage = "Could not upload image with id: $eventId")
                    )
                }

                val finalUrl = uploadResponse.body<UploadResponse>().url

                call.respond(HttpStatusCode.OK, PlaywrightResponse(success = true, url = finalUrl))
            } catch (e: Exception) {
                logger.error("Error in Playwright API: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError,
                    PlaywrightResponse(success = false, errorMessage = e.message)
                )
            }
        }
    }
}