package es.daw.parallaxbot.telegram

import com.github.kotlintelegrambot.Bot
import es.daw.parallaxbot.common.KoinLogger
import es.daw.parallaxbot.common.rootMessage
import es.daw.parallaxbot.telegram.module.telegramModule
import es.daw.parallaxbot.telegram.service.TelegramAlertConsumer
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.slf4j.LoggerFactory

/**
 * Telegram microservice entrypoint.
 */
fun main(args: Array<String>) {
    EngineMain.main(args)
}

/**
 * Configures dependency injection, starts bot runtime, and handles shutdown lifecycle.
 */
fun Application.module() {

    val logger = LoggerFactory.getLogger("Application")

    install(Koin) {
        logger(KoinLogger())
        printLogger(Level.NONE)
        modules(
            module { single { environment.config } },
            telegramModule
        )
    }

    val bot by inject<Bot>()
    logger.info("telegram bot started $bot")
    val consumer by inject<TelegramAlertConsumer>()

    launch(Dispatchers.Default) {
        runCatching {
            consumer.start()
        }.onFailure { error ->
            logger.error("--------------------------------------")
            logger.error("REASON: ${error.rootMessage()}")
            logger.error("--------------------------------------")
        }
    }

    monitor.subscribe(ApplicationStopped) {
        logger.info("Application stopped.")
        bot.stopPolling()
        consumer.stop()
    }
}