package es.daw.parallaxbot.playwright.service

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.ScreenshotType
import es.daw.parallaxbot.common.dto.EventDTO
import org.slf4j.LoggerFactory
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.lang.AutoCloseable

/**
 * Renders event templates in a headless browser and captures image artifacts.
 */
class PlaywrightService(
    private val templateEngine: TemplateEngine,
    private val browser: Browser
): AutoCloseable {
    private val logger = LoggerFactory.getLogger(PlaywrightService::class.java)

    private val cssContent = this::class.java.classLoader
        .getResourceAsStream("css/event-card.css")
        ?.bufferedReader()?.use { it.readText() } ?: ""

    /**
     * Produces a screenshot image for the provided event contract.
     *
     * @param event event payload used to render the HTML template.
     * @return JPEG bytes for the rendered card, or empty bytes when rendering fails.
     */
    fun generateEventImage(event: EventDTO): ByteArray {
        return runCatching {
            logger.info("Generating capture for event: ${event.id}")

            val context = Context().apply { setVariable("event", event) }
            val htmlContent = templateEngine.process("event-card", context)

            browser.newContext(
                Browser.NewContextOptions().setViewportSize(800,450)
            ).use { browserContext ->
                val page = browserContext.newPage()

                page.setContent(htmlContent)
                page.addStyleTag(Page.AddStyleTagOptions().setContent(cssContent))

                page.waitForLoadState()

                page.screenshot(Page.ScreenshotOptions().setType(ScreenshotType.JPEG))
            }
        }.getOrElse { e ->
            logger.error("Failed to generate image ${event.id}: ${e.message}")
            ByteArray(0)
        }
    }

    /**
     * Closes browser resources owned by this service.
     */
    override fun close() {
        logger.info("Closing playwright service")
        browser.close()
    }

}