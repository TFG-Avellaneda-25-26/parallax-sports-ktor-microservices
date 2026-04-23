package es.daw.parallaxbot.discord.commands

import es.daw.parallaxbot.common.dto.SportDTO
import es.daw.parallaxbot.discord.bot.ICommand
import es.daw.parallaxbot.discord.client.SpringDiscordAdminClient
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.slf4j.LoggerFactory

/**
 * Admin-only slash command used by guild managers to choose where alerts land
 * in their guild. Without {@code sport} it sets the guild-wide default channel;
 * with {@code sport} it upserts a per-sport channel override.
 */
class SetChannelCommand(
    private val adminClient: SpringDiscordAdminClient,
    private val sports: List<SportDTO>
) : ICommand {

    override val name: String = "parallax-setchannel"
    override val description: String = "Route alerts into this channel (per-sport optional)"

    override val options: List<OptionData> = listOf(
        OptionData(OptionType.STRING, "sport", "Only route this sport into this channel", false)
            .addChoices(
                sports.map { Command.Choice(it.name, it.key) }
            )
    )

    private val logger = LoggerFactory.getLogger(SetChannelCommand::class.java)

    override fun getCommandData(): SlashCommandData =
        Commands.slash(name, description)
            .addOptions(options)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
            .setContexts(InteractionContextType.GUILD)

    // -> Source: Guild admin /parallax-setchannel || Action: Persist channel choice (default or per-sport) || Strategy: ephemeral feedback
    override suspend fun execute(event: SlashCommandInteractionEvent) {
        event.deferReply(true).queue()

        val guild = event.guild
        val channel = event.channel
        if (guild == null) {
            event.hook.sendMessage("Run this command inside a guild channel.").setEphemeral(true).queue()
            return
        }

        val sportKey = event.getOption("sport")?.asString

        val ok = adminClient.upsertChannel(
            guildId = guild.id,
            channelId = channel.id,
            sportKey = sportKey,
            setByDiscordUserId = event.user.id
        )

        val message = when {
            !ok -> "Could not save the channel choice. Please try again or contact the operator."
            sportKey == null -> "This channel is now the default destination for Parallax alerts."
            else -> "Alerts for sport `$sportKey` will now be routed here."
        }
        event.hook.sendMessage(message).setEphemeral(true).queue()
    }
}
