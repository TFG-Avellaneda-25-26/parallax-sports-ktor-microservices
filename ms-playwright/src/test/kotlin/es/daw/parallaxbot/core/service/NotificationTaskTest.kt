package es.daw.parallaxbot.core.service

import es.daw.parallaxbot.common.NotificationProvider
import es.daw.parallaxbot.playwright.service.PlaywrightService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlinx.coroutines.test.runTest

class NotificationTaskTest {

    @Test
    fun `Test notification task`() = runTest {
        val eventService = mockk<EventService>()
        val playwrightService = mockk<PlaywrightService>(relaxed = true)

        val mockProvider1 = mockk<NotificationProvider>(relaxed = true)
        val mockProvider2 = mockk<NotificationProvider>(relaxed = true)
        val providers = listOf(mockProvider1, mockProvider2)

        coEvery { eventService.fetchPendingNotifications() } returns listOf(mockk(relaxed = true))

        val task = NotificationTask(eventService, playwrightService, providers)

        task.checkAndNotify()

        coVerify(exactly = 1) { mockProvider1.notify(any(), any()) }
        coVerify(exactly = 1) { mockProvider2.notify(any(), any()) }

        println("Success Core notified providers")
    }
}