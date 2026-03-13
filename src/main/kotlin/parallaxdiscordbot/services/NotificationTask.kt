package es.daw.parallaxdiscordbot.services

import es.daw.parallaxdiscordbot.bot.DiscordBotHandler
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class NotificationTask(
    private val eventService: EventService,
    private val playwrightService: PlaywrightService,
    private val discordBotHandler: DiscordBotHandler
) {
    private val logger = LoggerFactory.getLogger(NotificationTask::class.java)
    private val notifiedEvents = mutableSetOf<Long>()

    suspend fun start() {
        logger.info("Starting notification task... every minute")

        while (true) {
            try {
                logger.info("Notification task check...")
                val allEvents = eventService.getAllEvents()
                val now = LocalDateTime.now()

                val nextEvents = allEvents.filter { event->
                    val difference = ChronoUnit.MINUTES.between(now, event.dateTime)
                    difference in 0..30 && !notifiedEvents.contains(event.id)
                 }

                nextEvents.forEach { event ->
                    logger.info("Found event ${event.id}")

                    val byteImage = playwrightService.generateEventImage(event)
                    val message = "next event: ${event.eventName}"

                    discordBotHandler.sendEventNotification(byteImage, message)
                    event.id?.let { notifiedEvents.add(it) }
                }


            } catch (e: Exception) {
                logger.error("error during schedule notification ${e.message}")
            }

            delay(60000)
        }
    }
}