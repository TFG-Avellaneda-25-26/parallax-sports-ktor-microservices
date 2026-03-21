package es.daw.parallaxbot.common

import es.daw.parallaxbot.common.client.PlaywrightClient
import es.daw.parallaxbot.common.dto.AlertStatusCallback
import es.daw.parallaxbot.common.dto.AlertStreamMessage
import es.daw.parallaxbot.common.service.SpringCallbackService
import io.lettuce.core.Consumer
import io.lettuce.core.RedisClient
import io.lettuce.core.XReadArgs
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * Consumes alert messages from a Redis stream consumer group and delegates delivery to a channel-specific worker.
 *
 * This base class owns group creation, polling, ack semantics, retry loop behavior, and payload mapping.
 */
abstract class RedisStreamConsumer(
    private val redisClient: RedisClient,
    private val playwrightClient: PlaywrightClient,
    private val springCallbackService: SpringCallbackService,
    val streamName: String,
    val groupName: String,
    private val consumerName: String = "worker-${UUID.randomUUID()}"
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var isRunning = true

    abstract fun getWorkerType(): String
        /**
         * Processes one stream message.
         *
         * @param message normalized alert payload read from Redis stream fields.
         * @return true when artifact generation (if required), provider delivery, and callback status
         * reporting all succeed; false when any step fails and the message must remain pending.
         */
    suspend fun onMessageReceived(message: AlertStreamMessage): Boolean {

        val startTime = Clock.System.now()
            val workerId = "${getWorkerType()}-${Thread.currentThread().threadId()}"

        return try {
            val artifactUrl = if (message.artifactRequired) {
                val response = playwrightClient.generateEventScreenshot(message.eventId)

                if (!response.success || response.url == null) {
                    throw Exception("Playwright failed: ${response.errorMessage}")
                }
                response.url
            } else null

            val providerMessageId = sendToProvider(message, artifactUrl)

            springCallbackService.sendStatus(
                message.alertId,
                AlertStatusCallback(
                    status = "sent",
                    workerId = workerId,
                    providerMessageId = providerMessageId,
                    latencyMs = (Clock.System.now() - startTime).inWholeMilliseconds
                )
            )
            true
        } catch (e: Exception) {
            springCallbackService.sendStatus(
                message.alertId,
                AlertStatusCallback(
                    status = "failed-retryable",
                    workerId = workerId,
                    errorMessage = e.message
                )
            )
            false
        }
    }

    abstract suspend fun sendToProvider(message: AlertStreamMessage, artifactUrl: String?): String?

        /**
         * Starts the long-running poll loop for this consumer.
         *
            * Contract:
            * 1) Ensures the consumer group exists for the configured stream.
            * 2) Polls messages with blocking reads from the last-consumed offset.
            * 3) Maps stream fields to the alert DTO and delegates processing to channel-specific delivery.
            * 4) ACKs messages only when `onMessageReceived` returns true.
            * 5) Leaves failed messages unacked so they stay pending for retry/reclaim flows.
         */
    suspend fun start() = coroutineScope {
                /*============================================================
                    CONSUMER BOOTSTRAP
                    Connection setup and consumer-group initialization
                ============================================================*/
        logger.info("Starting consumer $consumerName for stream $streamName")

        val connection = redisClient.connect()
        val syncCommands = connection.sync()

        try {
            syncCommands.xgroupCreate(XReadArgs.StreamOffset.from(streamName, "0-0"), groupName)
        } catch (e: Exception) {
            logger.warn("Group $groupName already exists for stream $streamName")
        }

        logger.info("Started consumer $consumerName for stream $streamName")

                /*============================================================
                    MESSAGE LOOP
                    Read, transform, delegate, and ACK successful deliveries
                ============================================================*/
        while (isRunning) {
            try {
                val messages = syncCommands.xreadgroup(
                    Consumer.from(groupName, consumerName),
                    XReadArgs.Builder.block(5.seconds.toJavaDuration()).count(1),
                    XReadArgs.StreamOffset.lastConsumed(streamName)
                )

                if (messages.isEmpty()) continue

                for (message in messages ) {
                    val messageId = message.id

                    val dto = mapToDto(message.body)
                    val success = onMessageReceived(dto)

                    if (success) {
                        syncCommands.xack(streamName, groupName, messageId)
                        logger.info("Message $messageId processed and ACKed")
                    } else {
                        logger.warn("Message $messageId could not be processed, NO ACK sent")
                    }
                }
                delay(1000)
            } catch (e: Exception) {
                logger.error("Error in consumer $consumerName for stream $streamName: ${e.message}")
                delay(5.seconds)
            } finally {
                logger.info("Stopping consumer $consumerName for stream $streamName")
                connection.close()
            }
        }
        connection.close()
    }

    /**
     * Requests a graceful shutdown of the polling loop.
     */
    fun stop() {
        isRunning = false
    }

    /**
     * Maps Redis stream fields into the alert contract consumed by downstream workers.
     *
     * @param p raw Redis stream key-value fields.
     * @return alert payload with safe defaults for missing optional fields.
     */
    private fun mapToDto(p: Map<String, String>): AlertStreamMessage {
        return AlertStreamMessage(
            schemaVersion = p["schemaVersion"] ?: "v1",
            alertId = p["alertId"]?.toLong() ?: 0L,
            userId = p["userId"]?.toLong() ?: 0L,
            eventId = p["eventId"]?.toLong() ?: 0L,
            channel = p["channel"] ?: "unknown",
            sendAtUtc = p["sendAtUtc"] ?: "",
            idempotencyKey = p["idempotencyKey"] ?: "",
            attempts = p["attempts"]?.toInt() ?: 0,
            maxAttempts = p["maxAttempts"]?.toInt() ?: 3,
            artifactRequired = p["artifactRequired"]?.toBoolean() ?: false,
            artifactId = p["artifactId"]?.toLong(),
            eventName = p["eventName"],
            eventType = p["eventType"],
            eventStatus = p["eventStatus"],
            eventStartTimeUtc = p["eventStartTimeUtc"],
            eventEndTimeUtc = p["eventEndTimeUtc"],
            competitionName = p["competitionName"],
            venueName = p["venueName"],
            venueTimezone = p["venueTimezone"]
        )
    }
}