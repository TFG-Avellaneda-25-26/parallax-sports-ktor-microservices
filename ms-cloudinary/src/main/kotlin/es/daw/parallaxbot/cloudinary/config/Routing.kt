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
// -> Source: Ktor Routing Init || Action: Expose internal Cloudinary endpoints || Strategy: structured 200/400/500 response mapping
fun Application.configureRouting(cloudinaryService: CloudinaryService) {
    routing {
                /*============================================================
                    ARTIFACT LOOKUP
                    Resolve existing Cloudinary asset URL by event id
                ============================================================*/
                // -> Source: HTTP GET /check/{eventId} || Action: Read artifact metadata from Cloudinary-backed service || Strategy: return 400 on missing id, else 200 with existence contract
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

                /*============================================================
                    ARTIFACT UPLOAD
                    Parse multipart payload and persist image in Cloudinary
                ============================================================*/
                // -> Source: HTTP POST /upload || Action: Upload artifact bytes and return persisted URL || Strategy: validate multipart payload, return 400 on missing data, 500 on unexpected failures
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