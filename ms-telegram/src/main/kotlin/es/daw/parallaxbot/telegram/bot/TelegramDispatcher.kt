package es.daw.parallaxbot.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

/**
 * Resolves Telegram command handlers by name and executes them on a coroutine scope.
 */
class TelegramDispatcher(
    commandList: List<ITelegramCommand>,
    private val scope: CoroutineScope
) {
    private val commands = commandList.associateBy { it.name }
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Returns registered command names for Telegram command binding.
     *
     * @return command name set used by telegram bot dispatcher registration.
     */
    fun getCommandNames() = commands.keys

    /**
     * Dispatches one Telegram command invocation to its matching command handler.
     *
     * @param bot Telegram bot instance.
     * @param message incoming command message.
     * @param commandName resolved command name.
     * @param args command argument list.
     */
    fun dispatchCommand(bot: Bot, message: Message, commandName: String, args: List<String>) {
        logger.info("Executing command: $commandName")

        scope.launch {
            try {
                commands[commandName]?.execute(bot, message, args)
            } catch (e: Exception) {
                logger.error("Error in command $commandName", e)
            }
        }
    }
}