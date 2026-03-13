package es.daw.parallaxdiscordbot

import es.daw.parallaxdiscordbot.bot.DiscordListener
import es.daw.parallaxdiscordbot.bot.configureDiscordBot
import es.daw.parallaxdiscordbot.config.configureCommands
import es.daw.parallaxdiscordbot.config.configureTemplating
import es.daw.parallaxdiscordbot.config.createHttpClient
import es.daw.parallaxdiscordbot.config.loadConfig
import es.daw.parallaxdiscordbot.services.EventService
import es.daw.parallaxdiscordbot.services.NotificationTask
import es.daw.parallaxdiscordbot.services.PlaywrightService
import io.ktor.server.application.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")
    val config = loadConfig() // Object with properties from the config file (.env)

    val httpClient = createHttpClient(config.apiKey)
    val thymeLeafEngine = configureTemplating()

    val playwrightService = PlaywrightService(thymeLeafEngine)
    monitor.subscribe(ApplicationStopped) {
        playwrightService.close()
    }
    val eventService = EventService(httpClient, config.eventApiUrl)
    val commands = configureCommands(config, eventService)

    val discordListener = DiscordListener(commands, this)
    val discordBotHandler = configureDiscordBot(discordListener, config, commands)
    val notificationTask = NotificationTask(eventService,playwrightService, discordBotHandler)

    // PostConstruct task
    launch {
        logger.info("Starting discord bot...")
        eventService.fetchEvents()
        notificationTask.start()
    }
}
