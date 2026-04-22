package es.daw.parallaxbot.discord.commands

import es.daw.parallaxbot.discord.bot.ICommand
import es.daw.parallaxbot.discord.utils.EmbedFactory
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
            event.deferReply(true).queue()

            val embed = EmbedFactory.userAuth(event.user.name, event.user.avatarUrl)
                .addField("Link", authApiUrl, false)

            event.hook.sendMessageEmbeds(embed.build())
                .setEphemeral(true)
                .submit()
                .join()
        } catch (e: Exception) {
            logger.error(e.message)
            event.hook.sendMessage("error Sending login link: ${e.message}").submit().join()
        }
    }
}