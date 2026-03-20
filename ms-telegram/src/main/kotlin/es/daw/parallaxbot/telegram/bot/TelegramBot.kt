package es.daw.parallaxbot.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.BotCommand
import es.daw.parallaxbot.common.config.TelegramConfig
import org.slf4j.LoggerFactory

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