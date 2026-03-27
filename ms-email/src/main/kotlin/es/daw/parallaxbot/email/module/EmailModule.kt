package es.daw.parallaxbot.email.module

import es.daw.parallaxbot.common.client.PlaywrightClient
import es.daw.parallaxbot.common.config.configureEngine
import es.daw.parallaxbot.common.config.emailConfigModule
import es.daw.parallaxbot.common.config.networkModule
import es.daw.parallaxbot.common.config.redisModule
import es.daw.parallaxbot.common.service.SpringCallbackService
import es.daw.parallaxbot.email.service.EmailAlertConsumer
import es.daw.parallaxbot.email.service.EmailService
import es.daw.parallaxbot.email.service.GoogleTokenManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Wires email sender dependencies and configuration providers.
 */
val emailModule = module {
    singleOf(::EmailService)
    singleOf(::SpringCallbackService)
    singleOf(::PlaywrightClient)
    singleOf(::EmailAlertConsumer)
    singleOf(::configureEngine)
    singleOf(::GoogleTokenManager)
    includes(emailConfigModule, networkModule, redisModule)
}