package es.daw.parallaxbot.discord.bot

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface ICommand {
    val name: String
    val description: String
    val options: List<OptionData> get() = emptyList()
    suspend fun execute(event: SlashCommandInteractionEvent)
    fun getCommandData(): SlashCommandData {
        return Commands.slash(name, description).addOptions(options)
    }
}