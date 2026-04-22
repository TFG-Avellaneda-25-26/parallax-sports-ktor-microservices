package es.daw.parallaxbot.discord.service

import es.daw.parallaxbot.common.ProviderPermanentFailureException
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
 * Discord provider service: fetches event data and delivers rich embeds,
 * routing each alert to either a DM or a guild channel based on the
 * pre-resolved routing carried in the stream payload.
 */
class DiscordService(
    private val httpClient: HttpClient,
    private val discordConfig: DiscordConfig
): KoinComponent {
    private val logger = LoggerFactory.getLogger(DiscordService::class.java)
    private val jda: JDA by inject()

    // -> Source: Slash Command /events || Action: Query event API by league type || Strategy: return empty list on HTTP or transport failures
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

    // -> Source: Redis Stream Worker || Action: Publish event embed to DM or guild channel || Strategy: throw ProviderPermanentFailureException on DM/channel errors (log-only, no retry)
    fun sendEventEmbed(message: AlertStreamMessage, artifactUrl: String?): String {
        val embed = EmbedFactory.createEventEmbed(message, artifactUrl)

        return when (message.discordDeliveryMode) {
            "DM" -> sendDm(message, embed)
            "GUILD_CHANNEL" -> sendGuildChannel(message, embed)
            else -> {
                logger.warn(
                    "Discord alert {} arrived without a delivery mode — dropping permanently",
                    message.alertId
                )
                throw ProviderPermanentFailureException(
                    "discord_unroutable",
                    "Missing discordDeliveryMode in stream payload"
                )
            }
        }
    }

    private fun sendDm(message: AlertStreamMessage, embed: net.dv8tion.jda.api.entities.MessageEmbed): String {
        val discordUserId = message.discordUserId
        if (discordUserId.isNullOrBlank()) {
            throw ProviderPermanentFailureException("discord_unroutable", "Missing discordUserId for DM delivery")
        }
        return try {
            val user = jda.retrieveUserById(discordUserId).complete()
            val channel = user.openPrivateChannel().complete()
            val sent = channel.sendMessageEmbeds(embed).complete()
            logger.info("Alert ${message.alertId} delivered via DM to $discordUserId (messageId=${sent.id})")
            sent.id
        } catch (e: Exception) {
            logger.warn(
                "Discord DM delivery failed alertId={} discordUserId={} reason={}",
                message.alertId, discordUserId, e.message
            )
            throw ProviderPermanentFailureException("dm_closed", e.message ?: "DM delivery rejected")
        }
    }

    private fun sendGuildChannel(message: AlertStreamMessage, embed: net.dv8tion.jda.api.entities.MessageEmbed): String {
        val channelId = message.discordChannelId
        if (channelId.isNullOrBlank()) {
            throw ProviderPermanentFailureException(
                "discord_unroutable",
                "Missing discordChannelId for guild-channel delivery"
            )
        }
        val channel = jda.getTextChannelById(channelId)
            ?: throw ProviderPermanentFailureException(
                "channel_unavailable",
                "Discord channel $channelId not accessible"
            )

        return try {
            val sent = channel.sendMessageEmbeds(embed).complete()
            logger.info("Alert ${message.alertId} delivered to guild channel $channelId (messageId=${sent.id})")
            sent.id
        } catch (e: Exception) {
            logger.warn(
                "Discord channel delivery failed alertId={} channelId={} reason={}",
                message.alertId, channelId, e.message
            )
            throw ProviderPermanentFailureException("channel_unavailable", e.message ?: "Channel send rejected")
        }
    }
}
