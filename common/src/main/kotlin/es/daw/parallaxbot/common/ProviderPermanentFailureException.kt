package es.daw.parallaxbot.common

/**
 * Thrown by a worker when the provider has permanently rejected the delivery.
 *
 * The stream consumer acknowledges the message (no retry) and reports
 * {@code failed_permanent} to Spring with {@code errorCode}.
 */
class ProviderPermanentFailureException(
    val errorCode: String,
    message: String = errorCode
) : RuntimeException(message)
