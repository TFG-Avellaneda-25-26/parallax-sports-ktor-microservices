package es.daw.parallaxbot.discord.service

import es.daw.parallaxbot.common.RedisStreamConsumer
import es.daw.parallaxbot.common.client.PlaywrightClient
import es.daw.parallaxbot.common.dto.AlertStreamMessage
import es.daw.parallaxbot.common.service.SpringCallbackService
import io.lettuce.core.RedisClient

/**
 * Discord Alert Worker
 *
 * Stream: alerts.discord.v1
 * Group:  discord-workers
 * Role:   Dispatch Discord embed notifications for normalized alert messages.
 */
class DiscordAlertConsumer(
    redisClient: RedisClient,
    playwrightClient: PlaywrightClient,
    springCallbackService: SpringCallbackService,
    private val discordService: DiscordService
): RedisStreamConsumer(
    redisClient, playwrightClient, springCallbackService,
    streamName = "alerts.discord.v1",
    groupName = "discord-workers",
) {

    override fun getWorkerType() = "ktor-discord-worker"

    // -> Source: Redis Stream || Action: Send Discord embed notification || Strategy: return provider message id; DiscordService throws ProviderPermanentFailureException on unroutable/DM/channel errors
    override suspend fun sendToProvider(message: AlertStreamMessage, artifactUrl: String?): String {
        return discordService.sendEventEmbed(message, artifactUrl)
    }
}