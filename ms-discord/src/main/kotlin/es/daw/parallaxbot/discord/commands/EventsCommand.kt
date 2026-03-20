package es.daw.parallaxbot.discord.commands

import es.daw.parallaxbot.discord.bot.ICommand
import es.daw.parallaxbot.discord.service.DiscordService
import es.daw.parallaxbot.discord.utils.CommandOptions
import es.daw.parallaxbot.discord.utils.EmbedFactory
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.time.ZoneId

class EventsCommand(private val discordService: DiscordService) : ICommand {

    override val name: String = "events"
    override val description: String = "List upcoming events"
    override val options = listOf(CommandOptions.leagueType)

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        event.deferReply(false).queue()

        try {
            val eventType = event.getOption("type")?.asString ?: "ALL"

            val events = discordService.fetchEventsByType(eventType)

            if (events.isEmpty()) {
                event.hook.sendMessage("No events found for: $eventType").queue()
                return
            }

            val embedGroup = mutableListOf<MessageEmbed>()
            val groupedEvents = events.groupBy { it.eventType.uppercase() }

            groupedEvents.forEach { (type, list) ->
                val embed = EmbedFactory.leagueSchedule(type)

                    list.forEach { item ->
                        val unixTime = item.dateTime.atZone(ZoneId.systemDefault()).toEpochSecond()
                        embed.addField(
                            "Event date",
                            "<t:$unixTime:F>",
                            false
                        )
                    }
                embedGroup.add(embed.build())
            }

            embedGroup.chunked(10).forEach { chunk ->
                event.hook.sendMessageEmbeds(chunk).queue()
            }
        } catch (e: Exception) {
            event.hook.sendMessage("Error fetching events: ${e.message}").queue()
        }
    }
}