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
 * Registers Cloudinary API routes for artifact existence checks and uploads.
 *
 * @param cloudinaryService service handling Cloudinary IO operations.
 */
fun Application.configureRouting(cloudinaryService: CloudinaryService) {
    routing {
        /**
         * Checks whether an event artifact already exists in Cloudinary.
         */
        get("/check/{eventId}") {
            val eventId = call.parameters["eventId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

            val existingUrl = cloudinaryService.getExistingUrl(eventId)

            call.respond(
                HttpStatusCode.OK,
                CloudinaryCheckResponse(
                    exists = existingUrl != null,
                    url = existingUrl,
                    eventId = eventId
                )
            )
        }

        /**
         * Uploads one event artifact image and returns persisted URL metadata.
         */
        post("/upload") {
            try {
                val multipart = call.receiveMultipart()
                var fileBytes: ByteArray? = null
                var eventId: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            if (part.name == "eventId") eventId = part.value
                        }
                        is PartData.FileItem -> {
                            fileBytes = part.provider().readRemaining().readByteArray()
                        }
                        else -> part.dispose()
                    }
                }

                if (fileBytes == null || eventId == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, UploadResponse(false, errorMessage = "Data missing"))
                }

                val url = cloudinaryService.uploadImage(fileBytes, eventId)
                call.respond(HttpStatusCode.OK, UploadResponse(success = url != null, url = url, eventId = eventId))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, UploadResponse(false, errorMessage = e.message))
            }
        }
    }
}