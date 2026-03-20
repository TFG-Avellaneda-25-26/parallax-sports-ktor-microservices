package es.daw.parallaxbot.cloudinary.config

import es.daw.parallaxbot.cloudinary.service.CloudinaryService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting(cloudinaryService: CloudinaryService) {
    routing {
        get("/check/{eventId}") {
            val eventId = call.parameters["eventId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

            val existingUrl = cloudinaryService.getExistingUrl(eventId)

            if (existingUrl != null) {
                call.respond(HttpStatusCode.OK, mapOf("exists" to true, "url" to existingUrl))
            } else {
              call.respond(HttpStatusCode.OK, mapOf("exists" to false))
            }
        }
    }
}