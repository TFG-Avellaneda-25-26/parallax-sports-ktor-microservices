package es.daw.parallaxdiscordbot.config

import io.ktor.server.application.Application
import org.thymeleaf.TemplateEngine
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

fun Application.configureTemplating(): TemplateEngine {
    val engine = TemplateEngine().apply {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "templates/thymeleaf/"
            suffix = ".html"
            characterEncoding = "utf-8"
        })
    }

    return engine;
}