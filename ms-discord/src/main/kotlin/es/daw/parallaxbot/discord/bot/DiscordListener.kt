package es.daw.parallaxbot.discord.bot

import es.daw.parallaxbot.discord.client.SpringDiscordAdminClient
import es.daw.parallaxbot.discord.service.SportsCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.Command
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime

/**
 * JDA listener that resolves slash command handlers and propagates guild
 * lifecycle events to Spring so routing state stays consistent.
 */
class DiscordListener(
    commandList: List<ICommand>,
    private val scope: CoroutineScope,
    private val adminClient: SpringDiscordAdminClient,
    private val sportsCache: SportsCache
) : ListenerAdapter() {

    private val commandList = commandList.associateBy { it.name }
    private val logger = LoggerFactory.getLogger(DiscordListener::class.java)

    // -> Source: Discord Slash Interaction || Action: Dispatch mapped command handler || Strategy: async execution with command-level exception logging
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val commandName = event.name

        logger.info("Executing command $commandName")

        scope.launch {
            val startTime = System.currentTimeMillis()
            try {
                commandList[commandName]?.let { command ->
                    command.execute(event)
                    val duration = System.currentTimeMillis() - startTime
                    logger.info("Command $commandName completed in $duration ms")
                }
            } catch (e: Exception) {
                logger.error("Error while executing command $commandName", e)
            }
        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        if (event.name != "parallax-setchannel") return
        val focused = event.focusedOption
        if (focused.name != "sport") return

        scope.launch {
            try {
                val matches = sportsCache.search(focused.value, 25)
                val choices = matches.map { Command.Choice(it.name, it.key) }
                event.replyChoices(choices).queue()
            } catch (e: Exception) {
                logger.warn("Autocomplete failed for parallax-setchannel: ${e.message}")
                event.replyChoices(emptyList()).queue()
            }
        }
    }

    // -> Source: Bot added to guild || Action: Register guild with Spring and prompt admin to run /parallax-setchannel
    override fun onGuildJoin(event: GuildJoinEvent) {
        val guild = event.guild
        logger.info("Joined guild ${guild.name} (${guild.id})")
        scope.launch {
            adminClient.installGuild(
                guildId = guild.id,
                ownerDiscordId = guild.ownerId,
                installedAtIso = OffsetDateTime.now().toString()
            )
            promptSetup(guild)
        }
    }

    // -> Source: Bot removed from guild || Action: Remove guild state from Spring || Strategy: hard delete, no retries
    override fun onGuildLeave(event: GuildLeaveEvent) {
        val guildId = event.guild.id
        logger.info("Left guild ${event.guild.name} ($guildId)")
        scope.launch {
            adminClient.uninstallGuild(guildId)
        }
    }

    private fun promptSetup(guild: Guild) {
        val prompt = "Thanks for adding Parallax! Run `/parallax-setchannel` in the channel " +
            "that should receive alerts. Use `/parallax-setchannel sport:<key>` to route one sport " +
            "to a different channel."
        val owner = guild.owner?.user
        if (owner != null) {
            owner.openPrivateChannel()
                .flatMap { it.sendMessage(prompt) }
                .queue(
                    { logger.info("Setup prompt DMed to owner of ${guild.name}") },
                    { sendToSystemChannel(guild, prompt) }
                )
            return
        }
        sendToSystemChannel(guild, prompt)
    }

    private fun sendToSystemChannel(guild: Guild, message: String) {
        val channel = guild.systemChannel
        if (channel == null) {
            logger.info("No system channel available to prompt setup for ${guild.name}")
            return
        }
        channel.sendMessage(message).queue(
            { logger.info("Setup prompt posted to system channel of ${guild.name}") },
            { err -> logger.warn("Could not post setup prompt for ${guild.name}: ${err.message}") }
        )
    }
}
