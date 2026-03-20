package es.daw.parallaxbot.discord.module

import es.daw.parallaxbot.discord.bot.DiscordListener
import es.daw.parallaxbot.discord.bot.configureDiscordBot
import es.daw.parallaxbot.discord.service.DiscordService
import es.daw.parallaxbot.common.config.discordConfigModule
import es.daw.parallaxbot.common.config.networkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val discordModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    singleOf(::DiscordListener)
    singleOf(::configureDiscordBot)
    singleOf(::DiscordService)

    includes(commandModule, discordConfigModule, networkModule)
}