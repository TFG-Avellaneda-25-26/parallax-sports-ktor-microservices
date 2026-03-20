package es.daw.parallaxbot.email.config

import es.daw.parallaxbot.email.dto.EmailRequest
import es.daw.parallaxbot.email.service.EmailService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val emailService by inject<EmailService>()

    routing {
        post("/notify") {
            try {
                val request = call.receive<EmailRequest>()
                emailService.sendEventEmail(request.to,
                    request.event,
                    request.imageUrl,
                    request.accessToken
                )

                call.respond(HttpStatusCode.OK, mapOf("status" to "Email sent Successful"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}