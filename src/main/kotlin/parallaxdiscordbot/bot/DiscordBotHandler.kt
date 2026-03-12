package es.daw.parallaxdiscordbot.bot

import io.ktor.util.logging.Logger
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.utils.FileUpload
import org.slf4j.LoggerFactory

class DiscordBotHandler(
    private val jda: JDA,
    private val channelId: String,
) {
    fun sendEventNotification(imagesBytes: ByteArray, message: String) {
        val logger: Logger = LoggerFactory.getLogger("DiscordBotHandler")
        val channel = jda.getTextChannelById(channelId)

        if (channel != null) {
            val upload = FileUpload.fromData(imagesBytes, "event-card.png")

            channel.sendMessage(message)
                .addFiles(upload)
                .queue()
        } else {
            logger.error("Channel not found! $channelId")
        }
    }
}
