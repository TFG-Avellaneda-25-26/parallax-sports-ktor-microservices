package es.daw.parallaxbot.email

import es.daw.parallaxbot.common.KoinLogger
import es.daw.parallaxbot.common.config.networkModule
import es.daw.parallaxbot.common.rootMessage
import es.daw.parallaxbot.email.module.emailModule
import es.daw.parallaxbot.email.service.EmailAlertConsumer
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.logger.Level
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

/**
 * Email microservice entrypoint.
 */
fun main(args: Array<String>) {
    EngineMain.main(args)
}

/**
 * Configures DI, JSON serialization, email routes, and shutdown lifecycle logging.
 */
fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")

    install(Koin) {
        logger(KoinLogger())
        printLogger(Level.NONE)
        modules(
            module { single { environment.config } },
            emailModule
        )
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    val consumer by inject<EmailAlertConsumer>()

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
        logger.info("Email microservice Stopped")
        consumer.stop()
    }
}