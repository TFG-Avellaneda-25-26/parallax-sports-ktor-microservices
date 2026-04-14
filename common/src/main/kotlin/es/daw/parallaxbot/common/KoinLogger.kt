package es.daw.parallaxbot.common

import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.slf4j.LoggerFactory


/**
 * Minimal Koin logger that emits only error-level initialization failures.
 */
class KoinLogger : Logger(Level.ERROR) {
    private val logger = LoggerFactory.getLogger("Koin")

    override fun display(level: Level, msg: MESSAGE) {

        if (level == Level.ERROR) {
            val shortMsg = msg.split("\n").firstOrNull() ?: msg
            logger.error(shortMsg)
        }
    }
}