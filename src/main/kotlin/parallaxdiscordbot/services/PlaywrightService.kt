package es.daw.parallaxdiscordbot.services

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.ScreenshotType
import es.daw.parallaxdiscordbot.dto.EventDTO
import org.slf4j.LoggerFactory
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.lang.AutoCloseable
import java.nio.file.Paths

class PlaywrightService(private val templateEngine: TemplateEngine): AutoCloseable {
    private val logger = LoggerFactory.getLogger(PlaywrightService::class.java)

    // PlayWright Initialization
    private val playwright = Playwright.create()
    private val browser = playwright.chromium().launch(
        BrowserType.LaunchOptions().setHeadless(true)
    )

    fun generateEventImage(event: EventDTO): ByteArray {
        return runCatching {
            logger.info("Generating capture for event: ${event.eventName}")

            val context = Context().apply { setVariable("event", event) }
            val htmlContent = templateEngine.process("event-card", context)

            browser.newContext(
                Browser.NewContextOptions().setViewportSize(800,450)
            ).use { browserContext ->
                val page = browserContext.newPage()

                page.setContent(htmlContent)
                page.addStyleTag(Page.AddStyleTagOptions().setPath(
                    Paths.get("src/main/resources/static/css/event-card.css")
                ))

                page.waitForLoadState()
                page.screenshot(Page.ScreenshotOptions().setType(ScreenshotType.PNG))
            }
        }.getOrElse { e ->
            logger.error("Failed to generate image ${event.id}: ${e.message}")
            return ByteArray(0)
        }
    }

    override fun close() {
        logger.info("Closing playwright service")
        browser.close()
        playwright.close()
    }

}