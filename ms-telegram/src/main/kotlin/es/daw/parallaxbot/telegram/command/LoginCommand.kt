package es.daw.parallaxbot.telegram.command

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import es.daw.parallaxbot.telegram.bot.ITelegramCommand
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Sends account-linking messages for Telegram users.
 *
 * Current implementation uses a temporary fixed URL while auth-link generation is pending.
 */
class LoginCommand(private val authApiUrl: String) : ITelegramCommand {

    override val name: String = "login"
    override val description: String = "Link Telegram account with API"

    private val logger: Logger = LoggerFactory.getLogger(LoginCommand::class.java)

    /**
     * Sends an inline button with account-linking URL for the requesting Telegram user.
     *
     * @param bot Telegram bot instance used to send responses.
     * @param message source message containing user and chat context.
     * @param args command argument list (unused for login flow).
     */
    override suspend fun execute(bot: Bot, message: Message, args: List<String>) {
        val chatId = ChatId.fromId(message.chat.id)

        try {
            val telegramId = message.from?.id ?: throw Exception("Telegram id not found")
            val username = message.from?.firstName ?: "User"

            val token = UUID.randomUUID().toString()
            //val url = "${authApiUrl}?id=$telegramId&token=$token&platform=telegram"
            val url = "https://www.google.com"

            val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                listOf(
                    listOf(InlineKeyboardButton.Url(text = "Link account with Parallax", url = url))
                )
            )

            val result = bot.sendMessage(
                chatId = chatId,
                text = "Hi, $username! 👋\n\nTo link your account with the server, please click the button below. This link is for one-time use only.",
                replyMarkup = inlineKeyboardMarkup
            )

            result.fold(
                { success -> logger.info("Message success: ${success.messageId}") },
                { error -> logger.error("Error while processing message: $error") }
            )

            logger.info("Login link generated for user $telegramId")

        } catch (e: Exception) {
            logger.error("Error during login: ${e.message}")
            bot.sendMessage(
                chatId = chatId,
                text = "Error during login: ${e.message}",
            )
        }
    }
}