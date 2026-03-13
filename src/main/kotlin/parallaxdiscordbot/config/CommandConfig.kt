package es.daw.parallaxdiscordbot.config

import es.daw.parallaxdiscordbot.bot.ICommand
import es.daw.parallaxdiscordbot.commands.EventsCommand
import es.daw.parallaxdiscordbot.commands.LoginCommand
import es.daw.parallaxdiscordbot.services.EventService

fun configureCommands(
    config: AppConfig,
    eventService: EventService
): List<ICommand> {
    return listOf(
        EventsCommand(eventService),
        LoginCommand(config.authApiUrl)
    )
}