package es.daw.parallaxbot.telegram.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message

interface ITelegramCommand {
    val name: String
    val description: String

    suspend fun execute(bot: Bot, message: Message, args: List<String>)
}