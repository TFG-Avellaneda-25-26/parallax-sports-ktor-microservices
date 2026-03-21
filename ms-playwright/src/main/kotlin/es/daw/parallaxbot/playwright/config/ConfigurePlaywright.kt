package es.daw.parallaxbot.playwright.config

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright

/**
 * Creates a headless Chromium browser instance for screenshot rendering.
 *
 * @return launched browser used by the Playwright service.
 */
fun configurePlaywright(): Browser {
    val playwright =  Playwright.create()

    return playwright.chromium().launch(
        BrowserType.LaunchOptions().setHeadless(true)
    )
}