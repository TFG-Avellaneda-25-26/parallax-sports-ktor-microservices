package es.daw.parallaxdiscordbot.bot

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class DiscordListener(
    commandList: List<ICommand>,
    private val scope: CoroutineScope
) : ListenerAdapter() {

    private val commandList = commandList.associateBy { it.name }
    private val logger = LoggerFactory.getLogger(DiscordListener::class.java)

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
