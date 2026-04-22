package es.daw.parallaxbot.playwright.config

import es.daw.parallaxbot.common.dto.CloudinaryCheckResponse
import es.daw.parallaxbot.common.dto.PlaywrightResponse
import es.daw.parallaxbot.common.dto.ScreenshotRequest
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
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

/**
 * Registers Playwright internal routes used to generate or resolve event screenshot artifacts.
 */
fun Application.configureRouting() {
    val playwrightService by inject<PlaywrightService>()
    val httpClient by inject<HttpClient>()
    val config by inject<ApplicationConfig>()

    val renderBaseUrl = config.property("parallaxbot.api.endpoints.render").getString()
    val cloudinaryBaseUrl = config.property("parallaxbot.cloudinary-service.base-url").getString()

    val logger = LoggerFactory.getLogger("Routing")

    routing {
        post("/api/internal/screenshot") {
            try {
                val req = call.receive<ScreenshotRequest>()
                val eventId = req.eventId
                val channel = req.channel
                val hash = req.renderHash ?: "current"

                logger.info("Screenshot requested eventId=$eventId channel=$channel hash=$hash tz=${req.timezone}")

                val checkUrl = "$cloudinaryBaseUrl/check/$eventId/$hash"
                val check = httpClient.get(checkUrl).body<CloudinaryCheckResponse>()

                if (check.exists && check.url != null) {
                    return@post call.respond(
                        HttpStatusCode.OK,
                        PlaywrightResponse(success = true, url = check.url)
                    )
                }

                val tzParam = req.timezone?.let { "&tz=$it" } ?: ""
                val renderResponse = httpClient.get("$renderBaseUrl/$eventId?channel=$channel$tzParam")

                if (!renderResponse.status.isSuccess()) {
                    return@post call.respond(
                        HttpStatusCode.NotFound,
                        PlaywrightResponse(success = false, errorMessage = "Render failed for event $eventId: ${renderResponse.status}")
                    )
                }

                val html = renderResponse.body<String>()
                val effectiveHash = renderResponse.headers["X-Render-Hash"] ?: hash
                val imageBytes = playwrightService.renderHtmlToImage(html)

                if (imageBytes.isEmpty()) {
                    return@post call.respond(
                        HttpStatusCode.InternalServerError,
                        PlaywrightResponse(success = false, errorMessage = "Empty screenshot for event $eventId")
                    )
                }

                val uploadResponse = httpClient.post("$cloudinaryBaseUrl/upload") {
                    contentType(ContentType.MultiPart.FormData)
                    setBody(MultiPartFormDataContent(formData {
                        append("file", imageBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=\"${eventId}_${effectiveHash}.png\"")
                        })
                        append("eventId", eventId.toString())
                        append("hash", effectiveHash)
                    }))
                }

                if (!uploadResponse.status.isSuccess()) {
                    return@post call.respond(
                        HttpStatusCode.InternalServerError,
                        PlaywrightResponse(success = false, errorMessage = "Upload failed for event $eventId")
                    )
                }

                val finalUrl = uploadResponse.body<UploadResponse>().url
                call.respond(HttpStatusCode.OK, PlaywrightResponse(success = true, url = finalUrl))
            } catch (e: Exception) {
                logger.error("Error in Playwright API: ${e.message}", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    PlaywrightResponse(success = false, errorMessage = e.message)
                )
            }
        }
    }
}
