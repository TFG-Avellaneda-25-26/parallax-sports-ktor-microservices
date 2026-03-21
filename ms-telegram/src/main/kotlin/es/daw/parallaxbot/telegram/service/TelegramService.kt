package es.daw.parallaxbot.telegram.service

import es.daw.parallaxbot.common.config.TelegramConfig
import es.daw.parallaxbot.common.dto.AlertStreamMessage
import io.ktor.client.HttpClient
import org.koin.core.component.KoinComponent

/**
 * Telegram delivery service for outbound alert notifications.
 */
class TelegramService(
    httpClient: HttpClient,
    telegramConfig: TelegramConfig
): KoinComponent {
    /**
     * Placeholder delivery entry point for Telegram alert dispatch.
     *
     * Current implementation returns an empty provider identifier until Telegram send
     * integration is wired.
     *
     * @param message normalized alert payload from Redis stream.
     * @param artifactUrl optional artifact URL to include in the outbound message.
     * @return provider message identifier, currently an empty string placeholder.
     */
    fun sendEvent(message: AlertStreamMessage, artifactUrl: String?): String? {
        return ""
    }
}