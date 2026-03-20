package es.daw.parallaxbot.playwright.module

import es.daw.parallaxbot.common.config.networkModule
import es.daw.parallaxbot.common.config.playwrightConfigModule
import es.daw.parallaxbot.playwright.config.configureEngine
import es.daw.parallaxbot.playwright.config.configurePlaywright
import es.daw.parallaxbot.playwright.service.PlaywrightService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val playwrightModule = module {
    singleOf(::configurePlaywright)
    singleOf(::configureEngine)
    singleOf(::PlaywrightService)

    includes(playwrightConfigModule, networkModule)
}