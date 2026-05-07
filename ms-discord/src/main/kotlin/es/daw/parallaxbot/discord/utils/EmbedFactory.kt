package es.daw.parallaxbot.discord.utils

import es.daw.parallaxbot.common.dto.AlertStreamMessage
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
        val locale = message.userLocale?.takeIf { it.isNotBlank() }
            ?.let { Locale.forLanguageTag(it) } ?: Locale.ENGLISH
        val zone = message.userTimezone?.takeIf { it.isNotBlank() }
            ?.runCatching { ZoneId.of(this) }?.getOrNull() ?: ZoneId.of("UTC")

        val localizedTime = message.eventStartTimeUtc
            ?.runCatching { OffsetDateTime.parse(this).atZoneSameInstant(zone) }?.getOrNull()
            ?.format(DateTimeFormatter.ofPattern("EEE d MMM · HH:mm", locale))

        val description = buildString {
            append("El evento está por comenzar")
            if (localizedTime != null) {
                append("\n\n🕒 **").append(localizedTime).append("** (").append(zone.id).append(')')
            }
            message.competitionName?.takeIf { it.isNotBlank() }?.let {
                append("\n🏆 ").append(it)
            }
        }

        val builder = EmbedBuilder()
            .setTitle("🏁 ${message.eventName}")
            .setDescription(description)
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