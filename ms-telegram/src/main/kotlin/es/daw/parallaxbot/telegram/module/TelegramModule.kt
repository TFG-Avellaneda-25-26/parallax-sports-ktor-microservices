package es.daw.parallaxbot.telegram.module

import es.daw.parallaxbot.common.config.networkModule
import es.daw.parallaxbot.common.config.telegramConfigModule
import es.daw.parallaxbot.telegram.bot.TelegramDispatcher
import es.daw.parallaxbot.telegram.bot.configureTelegramBot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val telegramModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    singleOf(::configureTelegramBot)
    singleOf(::TelegramDispatcher)

    includes(commandModule, telegramConfigModule, networkModule)
}