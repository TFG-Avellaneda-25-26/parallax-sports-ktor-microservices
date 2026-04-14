package es.daw.parallaxbot.discord.module

import es.daw.parallaxbot.discord.bot.ICommand
import es.daw.parallaxbot.discord.commands.EventsCommand
import es.daw.parallaxbot.discord.commands.LoginCommand
import es.daw.parallaxbot.common.config.DiscordConfig
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Registers slash command implementations and publishes aggregate command list for bot startup.
 */
val commandModule = module {
    single { LoginCommand(get<DiscordConfig>().authApiUrl) }
    singleOf(::EventsCommand)

    single<List<ICommand>> {
        listOf(
            get<LoginCommand>(),
            get<EventsCommand>()
        )
    }
}