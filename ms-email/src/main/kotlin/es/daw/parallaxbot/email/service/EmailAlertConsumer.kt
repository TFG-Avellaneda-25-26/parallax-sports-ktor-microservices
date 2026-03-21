package es.daw.parallaxbot.email.service

import es.daw.parallaxbot.common.RedisStreamConsumer
import es.daw.parallaxbot.common.client.PlaywrightClient
import es.daw.parallaxbot.common.dto.AlertStreamMessage
import es.daw.parallaxbot.common.service.SpringCallbackService
import io.lettuce.core.RedisClient

/**
 * Email-channel Redis consumer that delegates alert delivery to the email provider service.
 */
class EmailAlertConsumer(
    redisClient: RedisClient,
    playwrightClient: PlaywrightClient,
    springCallbackService: SpringCallbackService,
    private val emailService: EmailService
): RedisStreamConsumer(
    redisClient, playwrightClient, springCallbackService,
    streamName = "alerts.email.v1",
    groupName = "email-workers"
) {
    /**
     * Provides the worker type value used in status callbacks and observability fields.
     *
     * @return stable worker type identifier for email deliveries.
     */
    override fun getWorkerType() = "ktor-email-worker"

    /**
     * Delegates one email alert to the provider-specific email service.
     *
     * @param message normalized alert payload consumed from Redis stream.
     * @param artifactUrl optional screenshot/artifact URL generated for this alert.
     * @return provider message identifier when available.
     */
    override suspend fun sendToProvider(message: AlertStreamMessage, artifactUrl: String?): String? {
        return emailService.sendEvent(message, artifactUrl).toString()
    }
}