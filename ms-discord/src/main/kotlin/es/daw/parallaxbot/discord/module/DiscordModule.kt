package es.daw.parallaxbot.discord.module

import es.daw.parallaxbot.common.client.PlaywrightClient
import es.daw.parallaxbot.discord.bot.DiscordListener
import es.daw.parallaxbot.discord.bot.configureDiscordBot
import es.daw.parallaxbot.discord.client.SpringDiscordAdminClient
import es.daw.parallaxbot.discord.service.DiscordService
import es.daw.parallaxbot.discord.service.SportsCache
import es.daw.parallaxbot.common.config.discordConfigModule
import es.daw.parallaxbot.common.config.networkModule
import es.daw.parallaxbot.common.config.redisModule
import es.daw.parallaxbot.common.service.SpringCallbackService
import es.daw.parallaxbot.discord.service.DiscordAlertConsumer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Dependency graph for Discord bot runtime, provider service, and Redis alert worker.
 */
val discordModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    singleOf(::SpringDiscordAdminClient)
    singleOf(::SportsCache)
    singleOf(::DiscordListener)
    singleOf(::SpringCallbackService)
    singleOf(::PlaywrightClient)
    singleOf(::configureDiscordBot)
    singleOf(::DiscordService)
    singleOf(::DiscordAlertConsumer)


    includes(commandModule, discordConfigModule, networkModule, redisModule)
}