package es.daw.parallaxbot.discord.config

import es.daw.parallaxbot.common.dto.EventDTO
import es.daw.parallaxbot.common.dto.NotificationRequest
import es.daw.parallaxbot.discord.service.DiscordService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val discordService by inject<DiscordService>()

    routing {
        post("/notify") {
            val request = call.receive<NotificationRequest>()

            discordService.notify(request.eventName, request.imageUrl)
            call.respond(HttpStatusCode.OK)
        }
    }
}