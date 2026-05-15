package es.daw.parallaxbot.common.observability

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

fun Application.installMetrics(serviceName: String): PrometheusMeterRegistry {
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    registry.config().commonTags("service", serviceName)

    ClassLoaderMetrics().bindTo(registry)
    JvmMemoryMetrics().bindTo(registry)
    JvmGcMetrics().bindTo(registry)
    JvmThreadMetrics().bindTo(registry)
    ProcessorMetrics().bindTo(registry)
    FileDescriptorMetrics().bindTo(registry)

    install(MicrometerMetrics) {
        this.registry = registry
        meterBinders = emptyList()
        timers { _, _ ->
            tags(listOf(Tag.of("service", serviceName)))
        }
    }

    routing {
        get("/metrics") {
            call.respondText(registry.scrape(), io.ktor.http.ContentType.Text.Plain)
        }
    }

    return registry
}
