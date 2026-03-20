package es.daw.parallaxbot.email.module

import es.daw.parallaxbot.common.config.MailConfig
import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.module

val configModule = module {
    single {
        val config = get<ApplicationConfig>()
        MailConfig(
            username = config.property("parallaxbot.email.username").getString(),
            from = config.property("parallaxbot.email.from").getString(),
        )
    }
}