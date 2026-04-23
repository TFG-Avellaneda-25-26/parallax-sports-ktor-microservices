package es.daw.parallaxbot.discord.module

import es.daw.parallaxbot.discord.bot.ICommand
import es.daw.parallaxbot.discord.commands.EventsCommand
import es.daw.parallaxbot.discord.commands.LinkCommand
import es.daw.parallaxbot.discord.commands.SetChannelCommand
import es.daw.parallaxbot.discord.commands.SetDeliveryCommand
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Registers slash command implementations and publishes aggregate command list for bot startup.
 */
val commandModule = module {
    singleOf(::LinkCommand)
    singleOf(::EventsCommand)
    singleOf(::SetChannelCommand)
    singleOf(::SetDeliveryCommand)

    single<List<ICommand>> {
        listOf(
            get<LinkCommand>(),
            get<EventsCommand>(),
            get<SetChannelCommand>(),
            get<SetDeliveryCommand>()
        )
    }
}