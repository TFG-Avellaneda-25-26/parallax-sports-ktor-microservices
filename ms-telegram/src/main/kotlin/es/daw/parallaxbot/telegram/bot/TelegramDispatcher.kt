package es.daw.parallaxbot.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class TelegramDispatcher(
    commandList: List<ITelegramCommand>,
    private val scope: CoroutineScope
) {
    private val commands = commandList.associateBy { it.name }
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getCommandNames() = commands.keys

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