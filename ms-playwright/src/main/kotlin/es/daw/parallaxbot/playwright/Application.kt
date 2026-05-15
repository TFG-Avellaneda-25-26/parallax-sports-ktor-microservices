package es.daw.parallaxbot.playwright

import es.daw.parallaxbot.common.observability.installHealth
import es.daw.parallaxbot.common.observability.installMetrics
import es.daw.parallaxbot.playwright.config.configureRouting
import es.daw.parallaxbot.playwright.module.playwrightModule
import es.daw.parallaxbot.playwright.service.PlaywrightService
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory

/**
 * Playwright microservice entrypoint.
 */
// -> Triggers: JVM service startup for ms-playwright || Contract: boots Ktor Netty engine
fun main(args: Array<String>) {
    EngineMain.main(args)
}

/**
 * Configures DI, internal screenshot routing, and browser resource shutdown.
 */
// -> Triggers: Ktor application initialization || Contract: wires DI, registers screenshot route, and closes browser resources on shutdown
fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")

    installMetrics("ms-playwright")
    installHealth()

    install(Koin) {
        slf4jLogger()
        modules(
            module {
                single { environment.config }
            },
            playwrightModule
        )
    }

    configureRouting()

    val playwrightService by inject<PlaywrightService>()

    monitor.subscribe(ApplicationStopped) {
        logger.info("Stopped playwright")
        playwrightService.close()
    }
}
