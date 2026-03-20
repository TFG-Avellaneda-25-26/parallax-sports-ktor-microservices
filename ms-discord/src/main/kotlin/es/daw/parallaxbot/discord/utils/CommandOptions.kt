package es.daw.parallaxbot.discord.utils

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

object CommandOptions {
    val leagueType = OptionData(OptionType.STRING, "type", "Select the league", false)
        .addChoice("LEC - Europe", "LEC")
        .addChoice("LVP - SuperLeague", "LVP")
        .addChoice("Worlds", "WORLDS")
}