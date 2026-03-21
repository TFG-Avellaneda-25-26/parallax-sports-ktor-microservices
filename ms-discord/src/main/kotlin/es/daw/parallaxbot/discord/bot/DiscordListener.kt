package es.daw.parallaxbot.discord.bot

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

/**
 * Routes incoming slash interactions to command implementations using a coroutine scope.
 */
class DiscordListener(
    commandList: List<ICommand>,
    private val scope: CoroutineScope
) : ListenerAdapter() {

    private val commandList = commandList.associateBy { it.name }
    private val logger = LoggerFactory.getLogger(DiscordListener::class.java)

    /**
     * Dispatches slash command interactions to the matching command handler.
     *
     * @param event incoming slash command interaction.
     */
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
