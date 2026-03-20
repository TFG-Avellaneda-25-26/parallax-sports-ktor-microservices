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

fun Application.configureRouting() {
    val playwrightService by inject<PlaywrightService>()
    val httpClient by inject<HttpClient>()

    val logger = LoggerFactory.getLogger("Routing")

    routing {
        post("/get-event-image") {
            try {
                val event = call.receive<EventDTO>()
                val eventId = event.id.toString()

                val check = httpClient.get("http://localhost:8085/check/$eventId").body<CloudinaryCheckResponse>()

                if (check.exists && check.url != null) {
                    return@post call.respond(
                        HttpStatusCode.OK,
                        PlaywrightResponse(
                            check.url!!,
                            eventId,
                            true
                        )
                    )
                }

                val imageBytes = playwrightService.generateEventImage(event)

                if (imageBytes.isEmpty()) {
                    logger.error("Empty image for event: $eventId")
                    return@post call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to render event image")
                    )
                }

                val response = httpClient.post("http://localhost:8085/upload") {
                    contentType(ContentType.MultiPart.FormData)
                    setBody(MultiPartFormDataContent(formData {
                        append("file", imageBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"$eventId.jpeg\"")
                        })
                        append("eventId", eventId)
                    }))
                }

                if (!response.status.isSuccess()) {
                    logger.error("Failed to upload image: ${response.status.description}")
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to upload image to cloudinary"))
                }

                val cloudUrl = response.body<UploadResponse>().url
                logger.info("Image uploaded: $cloudUrl")
                call.respond(HttpStatusCode.OK, PlaywrightResponse(cloudUrl, eventId, false))

            } catch (e: Exception) {
                logger.error("Error in Playwright routing: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, e)
            }
        }
    }
}