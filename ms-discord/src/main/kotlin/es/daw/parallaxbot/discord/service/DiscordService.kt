package es.daw.parallaxbot.discord.service

import es.daw.parallaxbot.common.config.DiscordConfig
import es.daw.parallaxbot.common.dto.AlertStreamMessage
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


/**
 * Coordinates Discord-facing operations for event retrieval and embed delivery.
 */
class DiscordService(
    private val httpClient: HttpClient,
    private val discordConfig: DiscordConfig
): KoinComponent {
    private val logger = LoggerFactory.getLogger(DiscordService::class.java)
    private val jda: JDA by inject()

    /**
     * Retrieves upcoming events filtered by event type.
     *
     * @param eventType event category filter consumed by the upstream events API.
     * @return list of events matching the filter; empty list when unavailable or request fails.
     */
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


    /**
     * Sends one event embed to the configured Discord channel.
     *
     * @param message alert payload used to render embed content.
     * @param artifactUrl optional screenshot URL attached to the embed.
     * @return provider message ID when sent, or null when delivery fails.
     */
    fun sendEventEmbed(message: AlertStreamMessage, artifactUrl: String?): String? {
        return try {
            val channelId = discordConfig.channelId
            val channel = jda.getTextChannelById(channelId)

            if (channel == null) {
                logger.error("Discord channel with ID $channelId not found")
                return null
            }
            val embed = EmbedFactory.createEventEmbed(message, artifactUrl)

            val discordMessage = channel.sendMessageEmbeds(embed).complete()

            logger.info("Alert ${message.alertId} sent to Discord. Message ID: ${discordMessage.id}")

            discordMessage.id
        } catch (e: Exception) {
            logger.error("Failed to send Discord embed: ${e.message}")
            null
        }
    }
}