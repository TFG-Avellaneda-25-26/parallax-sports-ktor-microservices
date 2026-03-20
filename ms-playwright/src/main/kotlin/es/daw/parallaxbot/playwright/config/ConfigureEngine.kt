package es.daw.parallaxbot.playwright.config

import org.thymeleaf.TemplateEngine
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

fun configureEngine(): TemplateEngine {
    return TemplateEngine().apply {
            setTemplateResolver(ClassLoaderTemplateResolver().apply {
                prefix = "templates/"
                suffix = ".html"
                characterEncoding = "utf-8"
            })
        }
}