package es.daw.parallaxbot.common.config

import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.module

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

val telegramConfigModule = module {
    single {
        val config = get<ApplicationConfig>()
        TelegramConfig(
            token = config.property("parallaxbot.telegram.token").getString(),
            authApiUrl = config.property("parallaxbot.api.endpoints.auth").getString() + "/telegram",
        )
    }
}

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

val emailConfigModule = module {
    single {

    }
}

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