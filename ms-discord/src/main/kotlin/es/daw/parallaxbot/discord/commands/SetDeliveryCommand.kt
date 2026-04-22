package es.daw.parallaxbot.discord.commands

import es.daw.parallaxbot.common.config.DiscordConfig
import es.daw.parallaxbot.discord.bot.ICommand
import es.daw.parallaxbot.discord.client.SpringDiscordAdminClient
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory

/**
 * User slash command to pick how alerts for this user should be delivered on
 * Discord. Without a sport argument it sets the user default; with a sport
 * argument it upserts a per-sport override. The {@code clear} option removes
 * the override and falls back to the default.
 */
class SetDeliveryCommand(
    private val adminClient: SpringDiscordAdminClient,
    private val discordConfig: DiscordConfig
) : ICommand, KoinComponent {

    override val name: String = "parallax-delivery"
    override val description: String = "Pick DM or channel delivery for your Parallax alerts"

    private val jda: JDA by inject()
    private val logger = LoggerFactory.getLogger(SetDeliveryCommand::class.java)

    override val options: List<OptionData> = listOf(
        OptionData(OptionType.STRING, "mode", "Where to send alerts", true)
            .addChoice("DM", "dm")
            .addChoice("Server channel", "channel"),
        OptionData(OptionType.STRING, "sport", "Apply only to this sport (key)", false),
        OptionData(OptionType.STRING, "guild", "Target guild id (required for channel if you share multiple)", false),
        OptionData(OptionType.BOOLEAN, "clear", "Clear the per-sport override and fall back to default", false)
    )

    // -> Source: User /parallax-delivery || Action: Upsert delivery preference or per-sport override || Strategy: ephemeral replies
    override suspend fun execute(event: SlashCommandInteractionEvent) {
        event.deferReply(true).queue()

        val discordUserId = event.user.id
        val userId = adminClient.resolveUserIdByDiscord(discordUserId)
        if (userId == null) {
            event.hook.sendMessage(
                "You have to link your Parallax account first. Run `/login` and follow the link."
            ).setEphemeral(true).queue()
            return
        }

        val sportKey = event.getOption("sport")?.asString
        val clear = event.getOption("clear")?.asBoolean ?: false

        if (clear) {
            if (sportKey == null) {
                event.hook.sendMessage("Use `clear:true` together with `sport:<key>` to remove a per-sport override.")
                    .setEphemeral(true).queue()
                return
            }
            val ok = adminClient.deleteUserSportDeliveryByKey(userId, sportKey)
            val msg = if (ok) "Removed per-sport override for `$sportKey`." else "Could not clear the override."
            event.hook.sendMessage(msg).setEphemeral(true).queue()
            return
        }

        val modeOption = event.getOption("mode")?.asString
        val mode = when (modeOption) {
            "dm" -> "DM"
            "channel" -> "GUILD_CHANNEL"
            else -> {
                event.hook.sendMessage("Mode must be `dm` or `channel`.").setEphemeral(true).queue()
                return
            }
        }

        val resolvedGuildId = if (mode == "GUILD_CHANNEL") {
            val explicit = event.getOption("guild")?.asString
            val guildId = explicit ?: inferSingleSharedGuild(discordUserId)
            if (guildId == null) {
                event.hook.sendMessage(
                    "You share multiple guilds with the bot. Add `guild:<id>` to pick one."
                ).setEphemeral(true).queue()
                return
            }
            guildId
        } else null

        val ok = if (sportKey == null) {
            adminClient.upsertUserDelivery(userId, mode, resolvedGuildId)
        } else {
            adminClient.upsertUserSportDeliveryByKey(userId, sportKey, mode, resolvedGuildId)
        }

        val scope = if (sportKey == null) "default" else "sport `$sportKey`"
        val target = if (mode == "DM") "DM" else "guild channel"
        val message = if (ok) {
            "Saved: $scope will now be delivered via $target."
        } else {
            "Could not save delivery preference. Please try again."
        }
        event.hook.sendMessage(message).setEphemeral(true).queue()
    }

    private fun inferSingleSharedGuild(discordUserId: String): String? {
        val sharedGuilds = jda.guilds.filter { it.getMemberById(discordUserId) != null }
        return if (sharedGuilds.size == 1) sharedGuilds.first().id else null
    }
}
