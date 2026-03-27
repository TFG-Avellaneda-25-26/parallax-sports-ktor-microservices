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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory

/**
 * Discord provider service that fetches event data and sends rich embeds to configured guild channels.
 */
class DiscordService(
    private val httpClient: HttpClient,
    private val discordConfig: DiscordConfig
): KoinComponent {
    private val logger = LoggerFactory.getLogger(DiscordService::class.java)
    private val jda: JDA by inject()

    // -> Source: Slash Command /events || Action: Query event API by league type || Strategy: return empty list on HTTP or transport failures
    // -> API: configured parallax event endpoint || Auth: internal API key strategy (configured client) || Scope: event list retrieval by type
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

    // -> Source: Redis Stream Worker || Action: Publish event embed to configured Discord channel || Strategy: return null when channel/provider send fails
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