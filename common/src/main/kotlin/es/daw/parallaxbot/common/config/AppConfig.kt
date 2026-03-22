package es.daw.parallaxbot.common.config

/**
 * Discord transport and upstream endpoint configuration.
 */
data class DiscordConfig(
    val serverId: String,
    val eventApiUrl: String,
    val authApiUrl: String,
    val apiKey: String,
    val token: String,
    val channelId: String
)

/**
 * Playwright capture-service and provider configuration.
 */
data class PlaywrightConfig(
    val eventApiUrl: String,
    val apiKey: String,
    val providers: List<String>
)

/**
 * Telegram bot credentials and auth endpoint configuration.
 */
data class TelegramConfig(
    val token: String,
    val authApiUrl: String
)

/**
 * Mail sender identity configuration.
 */
data class MailConfig(
    val username: String,
    val from: String,
)

/**
 * Cloudinary credentials used for artifact storage.
 */
data class CloudinaryConfig(
    val cloudName: String,
    val apiKey: String,
    val apiSecret: String
)
