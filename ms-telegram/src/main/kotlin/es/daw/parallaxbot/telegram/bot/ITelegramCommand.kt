package es.daw.parallaxbot.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message

/**
 * Contract implemented by Telegram bot commands.
 */
interface ITelegramCommand {
    val name: String
    val description: String

    /**
     * Executes command behavior for one Telegram message command invocation.
     *
     * @param bot Telegram bot instance used to send replies.
     * @param message incoming Telegram message.
     * @param args parsed command arguments.
     */
    suspend fun execute(bot: Bot, message: Message, args: List<String>)
}