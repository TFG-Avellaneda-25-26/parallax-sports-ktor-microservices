package es.daw.parallaxbot.email

import es.daw.parallaxbot.common.config.networkModule
import es.daw.parallaxbot.email.config.configureRouting
import es.daw.parallaxbot.email.module.emailModule
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
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
            emailModule,
            networkModule
        )
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    configureRouting()

    monitor.subscribe(ApplicationStopped) {
        logger.info("Email microservice Stopped")
    }
}