package es.daw.parallaxbot.common.config

/**
 * Discord provider settings used by bot and API integrations.
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
 * Playwright integration settings for event retrieval and provider routing.
 */
data class PlaywrightConfig(
    val eventApiUrl: String,
    val apiKey: String,
    val providers: List<String>
)

/**
 * Telegram provider settings for bot auth and external auth callback path.
 */
data class TelegramConfig(
    val token: String,
    val authApiUrl: String
)

/**
 * Email provider and OAuth client settings used by Gmail token flows.
 */
data class EmailConfig(
    val clientId: String,
    val clientSecret: String,
    val username: String,
    val from: String,
)

/**
 * Cloudinary credentials used for image upload and lookup operations.
 */
data class CloudinaryConfig(
    val cloudName: String,
    val apiKey: String,
    val apiSecret: String
)
