package es.daw.parallaxbot.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.BotCommand
import es.daw.parallaxbot.common.config.TelegramConfig
import org.slf4j.LoggerFactory

/**
 * Builds and starts the Telegram bot polling loop with dynamic command dispatch.
 *
 * @param config Telegram bot credentials and endpoint configuration.
 * @param dispatcher command dispatcher used by command handlers.
 * @return started bot instance.
 */
fun configureTelegramBot(
    config: TelegramConfig,
    dispatcher: TelegramDispatcher
): Bot {
    val logger = LoggerFactory.getLogger(TelegramDispatcher::class.java)
    val botInstance = bot {
        token = config.token

        dispatch {

            message {
                logger.info("Message received: ${message.text}")
            }
            dispatcher.getCommandNames().forEach { cmdName ->
                command(cmdName) {
                    println("executing command: $cmdName")
                    dispatcher.dispatchCommand(bot, message, cmdName, args)
                }
            }
        }
    }

    val commandsToRegister = dispatcher.getCommandNames().map { name ->
        BotCommand(command = name, description = "Execute command $name")
    }

    botInstance.setMyCommands(commandsToRegister)

    return botInstance.apply {
        startPolling()
    }
}