package es.daw.parallaxbot.telegram.module

import es.daw.parallaxbot.common.client.PlaywrightClient
import es.daw.parallaxbot.common.config.networkModule
import es.daw.parallaxbot.common.config.redisModule
import es.daw.parallaxbot.common.config.telegramConfigModule
import es.daw.parallaxbot.common.service.SpringCallbackService
import es.daw.parallaxbot.telegram.bot.TelegramDispatcher
import es.daw.parallaxbot.telegram.bot.configureTelegramBot
import es.daw.parallaxbot.telegram.service.TelegramAlertConsumer
import es.daw.parallaxbot.telegram.service.TelegramService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Wires Telegram runtime dependencies, command dispatcher, and bot bootstrap.
 */
val telegramModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    singleOf(::configureTelegramBot)
    singleOf(::TelegramDispatcher)
    singleOf(::SpringCallbackService)
    singleOf(::PlaywrightClient)
    singleOf(::TelegramService)
    singleOf(::TelegramAlertConsumer)

    includes(commandModule, telegramConfigModule, networkModule, redisModule)
}