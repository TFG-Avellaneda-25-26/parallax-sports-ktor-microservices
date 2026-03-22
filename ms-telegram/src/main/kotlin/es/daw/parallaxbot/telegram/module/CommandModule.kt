package es.daw.parallaxbot.telegram.module

import es.daw.parallaxbot.common.config.TelegramConfig
import es.daw.parallaxbot.telegram.bot.ITelegramCommand
import es.daw.parallaxbot.telegram.command.LoginCommand
import org.koin.dsl.module

/**
 * Registers Telegram command implementations and exposes them as command contract list.
 */
val commandModule = module {
    single { LoginCommand(get<TelegramConfig>().authApiUrl) }

    single<List<ITelegramCommand>> {
        listOf(get<LoginCommand>())
    }
}