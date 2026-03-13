package es.daw.parallaxdiscordbot.commands

import es.daw.parallaxdiscordbot.bot.ICommand
import es.daw.parallaxdiscordbot.utils.EmbedFactory
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

class LoginCommand(private val authApiUrl: String) : ICommand {

    override val name: String = "login"
    override val description: String = "Link Discord account with API"

    val logger: Logger = LoggerFactory.getLogger(LoginCommand::class.java)

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        try {
            val discordId = event.user.id
            val token = UUID.randomUUID().toString()

            val linkUrl = "${authApiUrl}?id=$discordId&token=$token"

            println(linkUrl)

            val embed = EmbedFactory.userAuth(event.user.name, event.user.avatarUrl)
                .addField("Link", linkUrl, false)

            event.replyEmbeds(embed.build())
                .setEphemeral(true)
                .queue()
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }
}