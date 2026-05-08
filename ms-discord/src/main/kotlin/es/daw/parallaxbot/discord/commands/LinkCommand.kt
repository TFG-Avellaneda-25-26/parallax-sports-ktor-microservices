package es.daw.parallaxbot.discord.commands

import es.daw.parallaxbot.common.config.DiscordConfig
import es.daw.parallaxbot.discord.bot.ICommand
import es.daw.parallaxbot.discord.utils.EmbedFactory
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LinkCommand(val config: DiscordConfig) : ICommand {

    override val name: String = "link"
    override val description: String = "Link Discord account with API"

    val logger: Logger = LoggerFactory.getLogger(LinkCommand::class.java)

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        try {
            event.deferReply(true).queue()

            val embed = EmbedFactory.userAuth(event.user.name, event.user.avatarUrl)
                .addField("Link", config.authApiUrl, false)

            event.hook.sendMessageEmbeds(embed.build())
                .setEphemeral(true)
                .submit()
                .join()
        } catch (e: Exception) {
            logger.error(e.message)
            event.hook.sendMessage("error Sending link: ${e.message}").submit().join()
        }
    }
}