package es.daw.parallaxbot.common.config

import io.ktor.server.config.ApplicationConfig
import io.lettuce.core.RedisClient
import org.koin.dsl.module

/**
 * Loads Discord provider configuration from environment-backed application config.
 */
val discordConfigModule = module {
    single {
        // -> Source: Service Startup || Action: Bind DiscordConfig into DI container || Strategy: fail-fast if required config key is missing
        val config = get<ApplicationConfig>()
        DiscordConfig(
            serverId = config.property("parallaxbot.discord.server-id").getString(),
            eventApiUrl = config.property("parallaxbot.api.endpoints.event").getString(),
            authApiUrl = config.property("parallaxbot.api.endpoints.auth").getString() + "/discord",
            apiKey = config.property("parallaxbot.api.key").getString(),
            token = config.property("parallaxbot.discord.token").getString(),
            channelId = config.property("parallaxbot.discord.channels.ping").getString()
        )
    }
}

/**
 * Loads Telegram provider configuration from environment-backed application config.
 */
val telegramConfigModule = module {
    single {
        // -> Source: Service Startup || Action: Bind TelegramConfig into DI container || Strategy: fail-fast if required config key is missing
        val config = get<ApplicationConfig>()
        TelegramConfig(
            token = config.property("parallaxbot.telegram.token").getString(),
            authApiUrl = config.property("parallaxbot.api.endpoints.auth").getString() + "/telegram",
        )
    }
}

/**
 * Loads Playwright integration configuration from environment-backed application config.
 */
val playwrightConfigModule = module {
    single {
        // -> Source: Service Startup || Action: Bind PlaywrightConfig into DI container || Strategy: fail-fast if required config key is missing
        val config = get<ApplicationConfig>()
        PlaywrightConfig(
            eventApiUrl = config.property("parallaxbot.api.endpoints.event").getString(),
            apiKey = config.property("parallaxbot.api.key").getString(),
            providers = config.property("parallaxbot.providers").getList()
        )
    }
}

/**
 * Loads email and OAuth client configuration from environment-backed application config.
 */
val emailConfigModule = module {
    single {
        // -> Source: Service Startup || Action: Bind EmailConfig into DI container || Strategy: fail-fast if required config key is missing
        val config = get<ApplicationConfig>()
        EmailConfig(
            clientId = config.property("parallaxbot.email.client.id").getString(),
            clientSecret = config.property("parallaxbot.email.client.secret").getString(),
            username = config.property("parallaxbot.email.username").getString(),
            from = config.property("parallaxbot.email.from").getString()
        )
    }
}

/**
 * Loads Cloudinary credentials from environment-backed application config.
 */
val cloudinaryConfigModule = module {
    single {
        // -> Source: Service Startup || Action: Bind CloudinaryConfig into DI container || Strategy: fail-fast if required config key is missing
        val config = get<ApplicationConfig>()
        CloudinaryConfig(
            cloudName = config.property("parallaxbot.cloudinary.cloudName").getString(),
            apiKey = config.property("parallaxbot.cloudinary.apiKey").getString(),
            apiSecret = config.property("parallaxbot.cloudinary.apiSecret").getString(),
        )
    }
}

/**
 * Creates Redis client singleton used by stream consumers and token storage.
 */
val redisModule = module {
    single {
        // -> Source: Service Startup || Action: Create shared Redis client || Strategy: singleton connection factory for stream and cache operations
        val config = get<ApplicationConfig>()
        val redisUrl = config.property("parallaxbot.redis.url").getString()

        RedisClient.create(redisUrl)
    }
}