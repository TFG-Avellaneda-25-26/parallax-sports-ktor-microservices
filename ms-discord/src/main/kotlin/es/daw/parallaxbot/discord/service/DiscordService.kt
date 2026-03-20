package es.daw.parallaxbot.discord.service

import es.daw.parallaxbot.common.config.DiscordConfig
import es.daw.parallaxbot.common.dto.EventDTO
import es.daw.parallaxbot.discord.utils.EmbedFactory
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.utils.FileUpload
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory


class DiscordService(
    private val httpClient: HttpClient,
    private val discordConfig: DiscordConfig
): KoinComponent {
    private val logger = LoggerFactory.getLogger(DiscordService::class.java)
    private val jda: JDA by inject()

    suspend fun fetchEventsByType(eventType: String): List<EventDTO> {
        return try {
            logger.info("Fetching events for type $eventType")

            val response = httpClient.get(discordConfig.eventApiUrl) {
                parameter("type", eventType.uppercase())
            }

            if (response.status.isSuccess()) {
                response.body<List<EventDTO>>()
            } else {
                logger.error("no found events: ${response.status}")
                emptyList()
            }
        } catch (e: Exception) {
            logger.error("${e.message}")
            emptyList()
        }
    }

    fun notify(eventName: String, imageUrl: String) {
        val channel = jda.getTextChannelById(discordConfig.channelId)

        if (channel != null) {
            val embed = EmbedFactory.eventCard(eventName, imageUrl).build()

            channel.sendMessageEmbeds(embed)
                .queue(
                    { logger.info("Successfully sent event card to channel ${channel.id}")},
                    { logger.error("Failed to send event card to channel ${channel.id}") }
                )
        } else {
            logger.error("channel ${discordConfig.channelId} not found")
        }
    }
}