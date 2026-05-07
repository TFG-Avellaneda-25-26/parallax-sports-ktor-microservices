package es.daw.parallaxbot.common

import es.daw.parallaxbot.common.client.PlaywrightClient
import es.daw.parallaxbot.common.dto.AlertStatusCallback
import es.daw.parallaxbot.common.dto.AlertStreamMessage
import es.daw.parallaxbot.common.mapper.mapToDto
import es.daw.parallaxbot.common.service.SpringCallbackService
import io.lettuce.core.Consumer
import io.lettuce.core.RedisClient
import io.lettuce.core.StreamMessage
import io.lettuce.core.XAddArgs
import io.lettuce.core.XGroupCreateArgs
import io.lettuce.core.XReadArgs
import io.lettuce.core.api.sync.RedisCommands
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.toJavaDuration

/**
 * Base Redis stream worker.
 *
 * Stream: dynamic per channel worker
 * Group: dynamic per channel worker
 * Role: poll alerts stream entries, resolve optional artifacts, dispatch provider work, and report callback status.
 */
abstract class RedisStreamConsumer(
    private val redisClient: RedisClient,
    private val playwrightClient: PlaywrightClient,
    private val springCallbackService: SpringCallbackService,
    val streamName: String,
    val groupName: String,
    baseName: String = "worker-discord-test"
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val consumerName = "$baseName-1"
    private var isRunning = true
    private val browserLimit = Semaphore(3)

    abstract fun getWorkerType(): String
    abstract suspend fun sendToProvider(message: AlertStreamMessage, artifactUrl: String?): String?

        /*============================================================
            REDIS STREAM CONSUMPTION
            Poll loop, consumer group setup, and concurrent message dispatch
        ============================================================*/
        // -> Source: Application Lifecycle || Action: Start Redis stream poll/dispatch loop || Strategy: Blocking reads with loop-level exception recovery
    suspend fun start() = coroutineScope {
        logger.info("Starting consumer $consumerName for stream $streamName")

        val connection = redisClient.connect()
        val syncCommands = connection.sync()

        setupConsumerGroup(syncCommands)

        var currentOffset = "0"
        while (isRunning) {
            try {
                val messages = fetchMessages(syncCommands, currentOffset)

                if (messages.isEmpty()) {
                    currentOffset = ">"
                    delay(1.seconds)
                    continue
                }

                messages.forEach { message ->
                    launch {
                        processSingleMessage(message, syncCommands)
                    }
                }
            } catch (e: Exception) {
                logger.error("Error in loop: ${e.message}")
            }
        }
        connection.close()
    }

    private fun setupConsumerGroup(syncCommands: RedisCommands<String, String>) {
        try {
            syncCommands.xgroupCreate(
                XReadArgs.StreamOffset.from(streamName, "0"),
                groupName,
                XGroupCreateArgs.Builder.mkstream(true)
            )
        } catch (_: Exception) {
            logger.debug("Group $groupName already exists")
        }
    }

    private fun fetchMessages(syncCommands: RedisCommands<String, String>, currentOffset: String): List<StreamMessage<String, String>> {
        return syncCommands.xreadgroup(
            Consumer.from(groupName, consumerName),
            XReadArgs.Builder.block(5.seconds.toJavaDuration()).count(3),
            XReadArgs.StreamOffset.from(streamName, currentOffset)
        ) ?: emptyList()
    }

    private suspend fun processSingleMessage(message: StreamMessage<String, String>, sync: RedisCommands<String, String>) {
        try {
            delay((100..500).random().toLong())
            val dto = mapToDto(message.body)

            if (onMessageReceived(dto)) {
                sync.xack(streamName, groupName, message.id)
                sync.xdel(streamName, message.id)
            } else {
                handleFailureAndRetry(sync, message, dto)
            }
        } catch (e: Exception) {
            logger.error("Error processing message: ${message.id}: ${e.message}")
        }
    }

    private suspend fun handleFailureAndRetry(sync: RedisCommands<String, String>, message: StreamMessage<String, String>, dto: AlertStreamMessage) {
        sync.xack(streamName, groupName, message.id)
        sync.xdel(streamName, message.id)

        logger.warn("Retrying ${dto.alertId} in 15s seconds...")
        delay(15.seconds)

        val nextAttempt = dto.attempts + 1
        val body = message.body.toMutableMap().apply {
            put("attempts", nextAttempt.toString())
        }

        sync.xadd(streamName, XAddArgs().maxlen(1000), body)
    }

        /*============================================================
            ALERT DELIVERY EXECUTION
            Max-attempt validation, provider dispatch, and callback reporting
        ============================================================*/
        // -> Source: Redis Stream || Action: Process one alert message and decide ack/retry || Strategy: Drop when max attempts reached, retry on provider/callback failures
    suspend fun onMessageReceived(message: AlertStreamMessage): Boolean {
        val startTime = Clock.System.now()
        val workerId = "${getWorkerType()}-${Thread.currentThread().threadId()}"

        if (message.attempts >= message.maxAttempts) {
            logger.error("Alert ${message.alertId} dropped! Max attempts (${message.maxAttempts}) exceeded!")
            return true
        }

        return try {
            val artifactUrl = getArtifactIfNeeded(message)
            val providerMessageId = sendToProvider(message, artifactUrl)

            reportStatusToSpring(message, "sent", workerId, providerMessageId, startTime)
            true
        } catch (e: ProviderPermanentFailureException) {
            logger.warn("Permanent failure on alert ${message.alertId} reason=${e.errorCode}")
            reportStatusToSpring(
                message,
                "failed_permanent",
                workerId,
                errorMessage = e.message,
                errorCode = e.errorCode
            )
            true
        } catch (e: Exception) {
            logger.error("Failed  alert: ${message.alertId}: ${e.message}")
            reportStatusToSpring(message, "failed-retryable", workerId, errorMessage = e.message)
            false
        }
    }

    // -> Source: Alert Payload || Action: Request screenshot artifact from Playwright || Strategy: Semaphore-limited concurrency and fail-fast on invalid response
    // -> API: /api/internal/screenshot || Auth: internal network || Scope: event artifact generation
    private suspend fun getArtifactIfNeeded(message: AlertStreamMessage): String? {
        if (!message.artifactRequired) return null

        val timezone = if (message.channel.equals("email", ignoreCase = true)) {
            message.userTimezone ?: message.venueTimezone
        } else {
            message.venueTimezone
        }

        return browserLimit.withPermit {
            val response = playwrightClient.generateEventScreenshot(
                eventId = message.eventId,
                channel = message.channel,
                timezone = timezone,
                renderHash = message.renderHash,
            )

            if (!response.success || response.url == null)
                throw Exception("Playwright failed: ${response.errorMessage}")

            response.url
        }
    }

    // -> Source: Delivery Outcome || Action: Post status callback to Spring API || Strategy: Non-blocking best effort (warn on callback failure)
    // -> API: /api/internal/alerts/{alertId}/status || Auth: internal network || Scope: alert status propagation
    private suspend fun reportStatusToSpring(
        message: AlertStreamMessage,
        status: String,
        workerId: String,
        providerMessageId: String? = null,
        startTime: Instant? = null,
        errorMessage: String? = null,
        errorCode: String? = null
    ) {
        val latency = startTime?.let { (Clock.System.now() - it).inWholeMilliseconds }

        runCatching {
            springCallbackService.sendStatus(
                message.alertId,
                AlertStatusCallback(
                    status = status,
                    workerId = workerId,
                    providerMessageId = providerMessageId,
                    latencyMs = latency,
                    errorMessage = errorMessage,
                    errorCode = errorCode
                )
            )
        }.onFailure {
            logger.warn("Spring API is offline. Status not reported for alert: ${message.alertId}")
        }
    }

    /**
     * Requests graceful loop termination for the running consumer instance.
     */
    fun stop() {
        isRunning = false
    }
}