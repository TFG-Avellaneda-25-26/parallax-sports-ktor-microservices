package es.daw.parallaxbot.discord.service

import es.daw.parallaxbot.common.RedisStreamConsumer
import es.daw.parallaxbot.common.client.PlaywrightClient
import es.daw.parallaxbot.common.dto.AlertStreamMessage
import es.daw.parallaxbot.common.service.SpringCallbackService
import io.lettuce.core.RedisClient

/**
 * Consumes Discord alert jobs, resolves required artifacts, sends embeds, and reports callback status.
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

    /**
     * Delegates one Discord alert to the provider-specific delivery service.
     *
     * @param message normalized alert payload consumed from Redis stream.
     * @param artifactUrl optional screenshot/artifact URL generated for this alert.
     * @return provider message identifier when available.
     */
    override suspend fun sendToProvider(message: AlertStreamMessage, artifactUrl: String?): String? {
        return discordService.sendEventEmbed(message, artifactUrl).toString()
    }
}