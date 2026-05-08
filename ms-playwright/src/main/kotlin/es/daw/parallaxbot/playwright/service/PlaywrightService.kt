package es.daw.parallaxbot.playwright.service

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.ScreenshotType
import com.microsoft.playwright.options.WaitForSelectorState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.lang.AutoCloseable

/**
 * Renders pre-built HTML in a headless browser and captures PNG screenshots.
 */
class PlaywrightService(
    private val browser: Browser,
): AutoCloseable {
    private val logger = LoggerFactory.getLogger(PlaywrightService::class.java)

    suspend fun renderHtmlToImage(htmlContent: String): ByteArray = withContext(Dispatchers.IO) {
        runCatching {
            browser.newContext(
                Browser.NewContextOptions().setViewportSize(1200, 630)
            ).use { browserContext ->
                val page = browserContext.newPage()
                page.setContent(htmlContent)
                page.waitForLoadState()
                page.waitForSelector(
                    ".card",
                    Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3000.0)
                )
                page.screenshot(Page.ScreenshotOptions().setType(ScreenshotType.PNG))
            }
        }.getOrElse { e ->
            logger.error("Failed to render HTML screenshot: ${e.message}", e)
            ByteArray(0)
        }
    }

    override fun close() {
        logger.info("Closing playwright service")
        browser.close()
    }
}
