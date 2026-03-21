package es.daw.parallaxbot.discord.utils

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

/**
 * Centralized slash command option definitions used by Discord commands.
 */
object CommandOptions {
    /**
     * Optional league type filter for event listing command.
     */
    val leagueType = OptionData(OptionType.STRING, "type", "Select the league", false)
        .addChoice("LEC - Europe", "LEC")
        .addChoice("LVP - SuperLeague", "LVP")
        .addChoice("Worlds", "WORLDS")
}