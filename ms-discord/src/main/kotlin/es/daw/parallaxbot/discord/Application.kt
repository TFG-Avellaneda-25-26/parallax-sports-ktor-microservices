package es.daw.parallaxbot.discord

import es.daw.parallaxbot.common.KoinLogger
import es.daw.parallaxbot.common.rootMessage
import es.daw.parallaxbot.discord.module.discordModule
import es.daw.parallaxbot.discord.service.DiscordAlertConsumer
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.JDA
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.slf4j.LoggerFactory
import org.koin.ktor.ext.inject

/**
 * Discord microservice entrypoint.
 */
fun main(args: Array<String>) {
    EngineMain.main(args)
}

/**
 * Configures serialization, dependency injection, HTTP routes, and Redis consumer lifecycle.
 */
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
        logger(KoinLogger())
        printLogger(Level.NONE)
        modules(
            module { single { environment.config } },
            discordModule
        )
    }

    val jda by inject<JDA>()
    logger.info("DiscordBot started: ${jda.selfUser.name}")
    val discordAlertConsumer by inject<DiscordAlertConsumer>()

    launch(Dispatchers.Default) {
        runCatching {
            logger.info("Starting redis Stream Consumer: ${discordAlertConsumer.streamName}")
            discordAlertConsumer.start()
        }.onFailure { error ->
            logger.error("--------------------------------------")
            logger.error("REASON: ${error.rootMessage()}")
            logger.error("--------------------------------------")

        }
    }

    monitor.subscribe(ApplicationStopped) {
        logger.info("Discord server stopped!")
        jda.shutdown()
        discordAlertConsumer.stop()
    }

    launch {
        logger.info("Starting discord bot...")
    }
}
