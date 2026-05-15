package es.daw.parallaxbot.common.observability

import kotlinx.coroutines.slf4j.MDCContext
import org.slf4j.MDC

/**
 * Builds an MDCContext element seeded with the given values (nulls dropped).
 */
fun alertMdc(
    alertId: String? = null,
    workerId: String? = null,
    channel: String? = null,
    traceId: String? = null,
): MDCContext {
    val map = mutableMapOf<String, String>()
    val existing = MDC.getCopyOfContextMap()
    if (existing != null) map.putAll(existing)
    alertId?.let { map["alertId"] = it }
    workerId?.let { map["workerId"] = it }
    channel?.let { map["channel"] = it }
    traceId?.let { map["traceId"] = it }
    return MDCContext(map)
}
