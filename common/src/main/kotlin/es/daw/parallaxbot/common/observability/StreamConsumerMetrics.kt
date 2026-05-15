package es.daw.parallaxbot.common.observability

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.time.Duration

/**
 * Meters for a Redis-stream-driven worker. One instance per stream/group.
 */
class StreamConsumerMetrics(
    private val registry: MeterRegistry,
    private val stream: String,
    private val workerType: String,
) {

    fun messageConsumed() {
        Counter.builder("stream.messages.consumed.total")
            .tags("stream", stream, "worker_type", workerType)
            .register(registry)
            .increment()
    }

    fun messageProcessed(outcome: String, duration: Duration) {
        Timer.builder("stream.message.processing.seconds")
            .tags("stream", stream, "worker_type", workerType, "outcome", outcome)
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry)
            .record(duration)
    }

    fun messageRetried() {
        Counter.builder("stream.message.retries.total")
            .tags("stream", stream, "worker_type", workerType)
            .register(registry)
            .increment()
    }

    fun messageDropped(reason: String) {
        Counter.builder("stream.message.dropped.total")
            .tags("stream", stream, "worker_type", workerType, "reason", reason)
            .register(registry)
            .increment()
    }

    fun providerSend(outcome: String, duration: Duration) {
        Timer.builder("provider.send.seconds")
            .tags("worker_type", workerType, "outcome", outcome)
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry)
            .record(duration)
    }

    fun artifactFetch(outcome: String, duration: Duration) {
        Timer.builder("artifact.fetch.seconds")
            .tags("worker_type", workerType, "outcome", outcome)
            .register(registry)
            .record(duration)
    }

    fun callbackToSpring(status: String, outcome: String) {
        Counter.builder("callback.to.spring.total")
            .tags("worker_type", workerType, "status", status, "outcome", outcome)
            .register(registry)
            .increment()
    }
}
