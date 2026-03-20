package es.daw.parallaxbot.discord

import es.daw.parallaxbot.discord.config.configureRouting
import es.daw.parallaxbot.discord.module.discordModule
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDA
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import org.koin.ktor.ext.inject

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        })

    }
    val logger = LoggerFactory.getLogger("Application")

    install(Koin) {
        slf4jLogger()
        modules(
            module { single { environment.config } },
            discordModule
        )
    }

    configureRouting()

    val jda by inject<JDA>()
    logger.info("DiscordBot started: ${jda.selfUser.name}")

    monitor.subscribe(ApplicationStopped) {
        logger.info("Discord server stopped!")
        jda.shutdown()
    }

    launch {
        logger.info("Starting discord bot...")
    }
}
