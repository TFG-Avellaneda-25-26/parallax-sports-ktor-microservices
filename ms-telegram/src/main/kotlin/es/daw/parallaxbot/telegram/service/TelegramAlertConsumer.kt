package es.daw.parallaxbot.telegram.service

import es.daw.parallaxbot.common.RedisStreamConsumer
import es.daw.parallaxbot.common.client.PlaywrightClient
import es.daw.parallaxbot.common.dto.AlertStreamMessage
import es.daw.parallaxbot.common.service.SpringCallbackService
import io.lettuce.core.RedisClient

/**
 * Telegram-channel Redis consumer that delegates delivery to Telegram provider logic.
 */
class TelegramAlertConsumer(
    redisClient: RedisClient,
    playwrightClient: PlaywrightClient,
    springCallbackService: SpringCallbackService,
    private val telegramService: TelegramService
): RedisStreamConsumer(
    redisClient, playwrightClient, springCallbackService,
    streamName = "alerts.telegram.v1",
    groupName = "telegram-workers"
) {
    /**
     * Provides the worker type value used in status callbacks and logs.
     *
     * @return stable worker type identifier for Telegram deliveries.
     */
    override fun getWorkerType() = "ktor-telegram-worker"

    /**
     * Delegates one Telegram alert to the provider-specific service implementation.
     *
     * @param message normalized alert payload consumed from Redis stream.
     * @param artifactUrl optional screenshot/artifact URL generated for this alert.
     * @return provider message identifier when available.
     */
    // -> Triggers: alert message ready for Telegram delivery || Contract: sends provider payload and returns provider message id
    override suspend fun sendToProvider(message: AlertStreamMessage, artifactUrl: String?): String? {
        return telegramService.sendEvent(message, artifactUrl).toString()
    }
}