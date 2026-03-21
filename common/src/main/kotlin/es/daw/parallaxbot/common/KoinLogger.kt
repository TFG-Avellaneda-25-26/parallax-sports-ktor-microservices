package es.daw.parallaxbot.common

import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.slf4j.LoggerFactory


/**
 * Koin logger adapter that forwards only error-level diagnostics to SLF4J.
 */
class KoinLogger : Logger(Level.ERROR) {
    private val logger = LoggerFactory.getLogger("Koin")

    /**
     * Emits compact one-line Koin error messages.
     *
     * @param level Koin log level for the emitted message.
     * @param msg raw Koin message payload.
     */
    override fun display(level: Level, msg: MESSAGE) {

        if (level == Level.ERROR) {
            val shortMsg = msg.split("\n").firstOrNull() ?: msg
            logger.error(shortMsg)
        }
    }
}