package es.daw.parallaxbot.telegram

import com.github.kotlintelegrambot.Bot
import es.daw.parallaxbot.telegram.module.telegramModule
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {

    val logger = LoggerFactory.getLogger("Application")

    install(Koin) {
        slf4jLogger()
        modules(
            module { single { environment.config } },
            telegramModule
        )
    }

    val bot by inject<Bot>()
    logger.info("telegram bot started $bot")

    monitor.subscribe(ApplicationStopped) {
        logger.info("Application stopped.")
        bot.stopPolling()
    }
}