package es.daw.parallaxbot.playwright.config

import org.thymeleaf.TemplateEngine
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

/**
 * Creates the template engine used to render event card HTML.
 *
 * @return template engine configured for classpath templates.
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