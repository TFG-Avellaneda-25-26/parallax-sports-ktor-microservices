package es.daw.parallaxbot.common.config

import org.thymeleaf.TemplateEngine
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

/**
 * Builds the Thymeleaf template engine used by services rendering HTML notifications.
 */
fun configureEngine(): TemplateEngine {
    return TemplateEngine().apply {
            setTemplateResolver(ClassLoaderTemplateResolver().apply {
                prefix = "templates/"
                suffix = ".html"
                characterEncoding = "utf-8"
            })
        }
}