package es.daw.parallaxbot.common.config

import io.ktor.server.config.ApplicationConfig
import io.lettuce.core.RedisClient
import org.koin.dsl.module


val discordConfigModule = module {
    single {
        // -> Source: Service Startup || Action: Bind DiscordConfig into DI container || Strategy: fail-fast if required config key is missing
        val config = get<ApplicationConfig>()
        DiscordConfig(
            eventApiUrl = config.property("parallaxbot.api.endpoints.event").getString(),
            authApiUrl = config.property("parallaxbot.api.endpoints.auth").getString() + "/discord",
            discordAdminApiUrl = config.property("parallaxbot.api.endpoints.internal-discord").getString(),
            apiKey = config.property("parallaxbot.api.key").getString(),
            token = config.property("parallaxbot.discord.token").getString()
        )
    }
}

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

val emailConfigModule = module {
    single {
        // -> Source: Service Startup || Action: Bind EmailConfig into DI container || Strategy: fail-fast if required config key is missing
        val config = get<ApplicationConfig>()
        EmailConfig(
            clientId = config.property("parallaxbot.email.client.id").getString(),
            clientSecret = config.property("parallaxbot.email.client.secret").getString(),
            username = config.property("parallaxbot.email.username").getString(),
            from = config.property("parallaxbot.email.from").getString(),
            oauthRedirectUri = config.property("parallaxbot.email.oauth.redirect-uri").getString()
        )
    }
}

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

val redisModule = module {
    single {
        // -> Source: Service Startup || Action: Create shared Redis client || Strategy: singleton connection factory for stream and cache operations
        val config = get<ApplicationConfig>()
        val redisUrl = config.property("parallaxbot.redis.url").getString()

        RedisClient.create(redisUrl)
    }
}