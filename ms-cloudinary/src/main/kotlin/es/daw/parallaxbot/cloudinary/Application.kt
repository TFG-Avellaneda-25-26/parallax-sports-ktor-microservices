package es.daw.parallaxbot.cloudinary

import es.daw.parallaxbot.cloudinary.config.configureRouting
import es.daw.parallaxbot.cloudinary.module.cloudinaryModule
import es.daw.parallaxbot.cloudinary.service.CloudinaryService
import es.daw.parallaxbot.common.config.networkModule
import es.daw.parallaxbot.common.observability.installHealth
import es.daw.parallaxbot.common.observability.installMetrics
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory

/**
 * Cloudinary microservice entrypoint.
 */
// -> Source: JVM Startup || Action: Boot Ktor Netty application for Cloudinary service || Strategy: fail-fast on startup exceptions
fun main(args: Array<String>) {
    EngineMain.main(args)
}

/**
 * Configures serialization, dependency injection, and route registration for Cloudinary operations.
 */
// -> Source: Ktor Application Init || Action: Register DI modules and internal Cloudinary routes || Strategy: singleton wiring with graceful stop logging
fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")

    installMetrics("ms-cloudinary")
    installHealth()

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        })

    }

    install(Koin) {
        slf4jLogger()
        modules(
            module { single { environment.config } },
            cloudinaryModule
        )
    }

    val cloudinaryService by inject<CloudinaryService>()

    monitor.subscribe(ApplicationStopped) {
        logger.info("Cloudinary stopped.")
    }

    configureRouting(cloudinaryService)
}