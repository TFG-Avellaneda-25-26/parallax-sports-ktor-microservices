package es.daw.parallaxbot.common.config

import io.ktor.server.config.ApplicationConfig
import io.lettuce.core.RedisClient
import org.koin.dsl.module

/**
 * Provides Discord runtime configuration from application properties.
 */
val discordConfigModule = module {
    single {
        val config = get<ApplicationConfig>()
        DiscordConfig(
            serverId = config.property("parallaxbot.discord.server-id").getString(),
            eventApiUrl = config.property("parallaxbot.api.endpoints.event").getString(),
            authApiUrl = config.property("parallaxbot.api.endpoints.auth").getString() + "/discord",
            apiKey = config.property("parallaxbot.api.key").getString(),
            token = config.property("parallaxbot.discord.token").getString(),
            channelId = config.property("parallaxbot.discord.channels.main").getString()
        )
    }
}

/**
 * Provides Telegram runtime configuration from application properties.
 */
val telegramConfigModule = module {
    single {
        val config = get<ApplicationConfig>()
        TelegramConfig(
            token = config.property("parallaxbot.telegram.token").getString(),
            authApiUrl = config.property("parallaxbot.api.endpoints.auth").getString() + "/telegram",
        )
    }
}

/**
 * Provides Playwright runtime configuration from application properties.
 */
val playwrightConfigModule = module {
    single {
        val config = get<ApplicationConfig>()
        PlaywrightConfig(
            eventApiUrl = config.property("parallaxbot.api.endpoints.event").getString(),
            apiKey = config.property("parallaxbot.api.key").getString(),
            providers = config.property("parallaxbot.providers").getList()
        )
    }
}

/**
 * Reserved email module slot for parity with other provider modules.
 */
val emailConfigModule = module {
    single {
        val config = get<ApplicationConfig>()
        MailConfig(
            username = config.property("parallaxbot.email.username").getString(),
            from = config.property("parallaxbot.email.from").getString(),
        )
    }
}

/**
 * Provides Cloudinary credentials and tenant settings from application properties.
 */
val cloudinaryConfigModule = module {
    single {
        val config = get<ApplicationConfig>()
        CloudinaryConfig(
            cloudName = config.property("parallaxbot.cloudinary.cloudName").getString(),
            apiKey = config.property("parallaxbot.cloudinary.apiKey").getString(),
            apiSecret = config.property("parallaxbot.cloudinary.apiSecret").getString(),
        )
    }
}

/**
 * Creates a singleton Redis client using the configured connection URL.
 */
val redisModule = module {
    single {
        val config = get<ApplicationConfig>()
        val redisUrl = config.property("parallaxbot.redis.url").getString()

        RedisClient.create(redisUrl)
    }
}