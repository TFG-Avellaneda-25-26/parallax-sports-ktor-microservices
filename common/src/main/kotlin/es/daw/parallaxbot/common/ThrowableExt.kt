package es.daw.parallaxbot.common

/**
 * Returns the deepest meaningful error message, preferring IllegalArgument/IllegalState causes.
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