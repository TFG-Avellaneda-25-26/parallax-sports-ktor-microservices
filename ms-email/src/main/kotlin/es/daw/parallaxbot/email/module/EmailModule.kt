package es.daw.parallaxbot.email.module

import es.daw.parallaxbot.email.config.configureMailer
import es.daw.parallaxbot.email.service.EmailService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val emailModule = module {
    singleOf(::configureMailer)
    singleOf(::EmailService)
    includes(configModule)
}