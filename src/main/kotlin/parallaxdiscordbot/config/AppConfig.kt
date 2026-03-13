package es.daw.parallaxdiscordbot.config

import io.ktor.server.application.Application

data class AppConfig(
    val serverId: String,
    val eventApiUrl: String,
    val authApiUrl: String,
    val apiKey: String,
    val token: String,
    val channelId: String
)

fun Application.loadConfig(): AppConfig {
    return AppConfig(
        serverId = environment.config.property("discord.server-id").getString(),
        eventApiUrl = environment.config.property("api.endpoints.event").getString(),
        authApiUrl = environment.config.property("api.endpoints.auth").getString(),
        apiKey = environment.config.property("api.key").getString(),
        token = environment.config.property("discord.token").getString(),
        channelId = environment.config.property("discord.channels.main").getString()
    )
}