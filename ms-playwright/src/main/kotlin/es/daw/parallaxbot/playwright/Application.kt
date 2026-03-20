package es.daw.parallaxbot.playwright

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

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")

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
