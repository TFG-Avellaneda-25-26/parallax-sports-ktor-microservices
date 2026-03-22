package es.daw.parallaxbot.common

/**
 * Resolves the most relevant nested exception message for user-safe logging.
 *
 * @return message from the deepest meaningful cause or a stable fallback when unavailable.
 */
fun Throwable.rootMessage(): String {
    var current = this

    while (current.cause != null && current.cause != current) {
        val next = current.cause!!

        if (next is IllegalArgumentException || next is IllegalStateException) {
            return next.message ?: "Unknown Error"
        }
        current = next
    }
    return current.message ?: "Unknown Error"
}