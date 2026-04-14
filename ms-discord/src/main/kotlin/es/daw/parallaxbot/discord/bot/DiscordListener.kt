package es.daw.parallaxbot.discord.bot

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

/**
 * JDA listener that resolves slash command handlers and executes them in coroutine scope.
 */
class DiscordListener(
    commandList: List<ICommand>,
    private val scope: CoroutineScope
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
}
