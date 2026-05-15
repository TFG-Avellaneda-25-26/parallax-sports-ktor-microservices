package es.daw.parallaxbot.common.observability

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.installHealth() {
    routing {
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
        }
        get("/health/ready") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "READY"))
        }
    }
}
