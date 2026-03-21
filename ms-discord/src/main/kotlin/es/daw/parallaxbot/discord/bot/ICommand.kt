package es.daw.parallaxbot.discord.bot

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

/**
 * Contract implemented by all Discord slash commands.
 */
interface ICommand {
    val name: String
    val description: String
    val options: List<OptionData> get() = emptyList()

    /**
     * Executes command behavior for one slash interaction.
     *
     * @param event incoming Discord slash command interaction.
     */
    suspend fun execute(event: SlashCommandInteractionEvent)

    /**
     * Converts command metadata into JDA command registration payload.
     *
     * @return slash command data including option definitions.
     */
    fun getCommandData(): SlashCommandData {
        return Commands.slash(name, description).addOptions(options)
    }
}