package es.daw.parallaxdiscordbot.utils

import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color
import java.time.Instant

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
}