package es.daw.parallaxbot.common.config

data class DiscordConfig(
    val eventApiUrl: String,
    val authApiUrl: String,
    val discordAdminApiUrl: String,
    val apiKey: String,
    val token: String,
    val devGuild: String
)

data class PlaywrightConfig(
    val eventApiUrl: String,
    val apiKey: String,
    val providers: List<String>
)

data class TelegramConfig(
    val token: String,
    val authApiUrl: String
)

data class EmailConfig(
    val clientId: String,
    val clientSecret: String,
    val username: String,
    val from: String,
    val oauthRedirectUri: String,
)

data class CloudinaryConfig(
    val cloudName: String,
    val apiKey: String,
    val apiSecret: String
)
