package es.daw.parallaxbot.cloudinary.config

import es.daw.parallaxbot.cloudinary.service.CloudinaryService
import es.daw.parallaxbot.common.dto.CloudinaryCheckResponse
import es.daw.parallaxbot.common.dto.UploadResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import kotlinx.io.readByteArray
import io.ktor.server.application.Application
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.utils.io.readRemaining

/**
 * Registers internal HTTP endpoints for Cloudinary artifact lookup and upload.
 */
fun Application.configureRouting(cloudinaryService: CloudinaryService) {
    routing {
        get("/check/{eventId}/{hash}") {
            val eventId = call.parameters["eventId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val hash = call.parameters["hash"] ?: return@get call.respond(HttpStatusCode.BadRequest)

            val existingUrl = cloudinaryService.getExistingUrl(eventId, hash)

            call.respond(
                HttpStatusCode.OK,
                CloudinaryCheckResponse(
                    exists = existingUrl != null,
                    url = existingUrl,
                    eventId = eventId
                )
            )
        }

        post("/upload") {
            try {
                val multipart = call.receiveMultipart()
                var fileBytes: ByteArray? = null
                var eventId: String? = null
                var hash: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "eventId" -> eventId = part.value
                                "hash" -> hash = part.value
                            }
                        }
                        is PartData.FileItem -> {
                            fileBytes = part.provider().readRemaining().readByteArray()
                        }
                        else -> part.dispose()
                    }
                }

                if (fileBytes == null || eventId == null || hash == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, UploadResponse(false, errorMessage = "Data missing"))
                }

                val url = cloudinaryService.uploadImage(fileBytes, eventId, hash)
                call.respond(HttpStatusCode.OK, UploadResponse(success = url != null, url = url, eventId = eventId))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, UploadResponse(false, errorMessage = e.message))
            }
        }
    }
}
