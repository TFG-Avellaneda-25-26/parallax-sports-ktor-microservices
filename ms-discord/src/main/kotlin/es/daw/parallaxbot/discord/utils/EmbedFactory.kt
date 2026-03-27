package es.daw.parallaxbot.discord.utils

import es.daw.parallaxbot.common.dto.AlertStreamMessage
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.time.Instant

/**
 * Centralized factory for Discord embeds used by command and alert delivery flows.
 */
object EmbedFactory {

    private val ICON = null
    private val DEFAULT_COLOR = Color.decode("#2F3126")

    private fun base(): EmbedBuilder {
        return EmbedBuilder()
            .setTimestamp(Instant.now())
            .setFooter("ParallaxBot + Gaming Alerts", ICON)
    }

    fun leagueSchedule(type: String): EmbedBuilder {
        val color = when (type.uppercase()) {
            "LEC" -> Color.ORANGE
            "LVP" -> Color.BLUE
            "WORLDS" -> Color.YELLOW
            else -> Color.CYAN
        }

        return base()
            .setTitle("$type Schedule")
            .setColor(color)
            .setThumbnail("https://lol-logo-url.png")
    }

    fun userAuth(userName: String, avatarUrl: String?): EmbedBuilder {
        return base()
            .setTitle("User Authentication")
            .setAuthor(userName, null, avatarUrl)
            .setColor(Color.GREEN)
            .setDescription("Please follow the link below to link your account")
    }

    fun error(message: String): EmbedBuilder {
        return base()
            .setTitle("Error")
            .setDescription(message)
            .setColor(Color.RED)
    }

    fun createEventEmbed(message: AlertStreamMessage, imageUrl: String?): MessageEmbed {
        val builder = EmbedBuilder()
            .setTitle("🏁 ${message.eventName}")
            .setDescription("El evento está por comenzar")
            .setColor(Color.RED)

        if (!message.sendAtUtc.isNullOrBlank()) {
            try {
                builder.setTimestamp(Instant.parse(message.sendAtUtc))
            } catch (e: Exception) {
                builder.setTimestamp(Instant.now())
            }
        } else {
            builder.setTimestamp(Instant.now())
        }

        if (imageUrl != null) {
            builder.setImage(imageUrl)
        }

        return builder.build()
    }
}