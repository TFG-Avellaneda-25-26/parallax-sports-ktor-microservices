package es.daw.parallaxbot.common.config

data class DiscordConfig(
    val serverId: String,
    val eventApiUrl: String,
    val authApiUrl: String,
    val apiKey: String,
    val token: String,
    val channelId: String
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

data class MailConfig(
    val username: String,
    val from: String,
)

data class CloudinaryConfig(
    val cloudName: String,
    val apiKey: String,
    val apiSecret: String
)
