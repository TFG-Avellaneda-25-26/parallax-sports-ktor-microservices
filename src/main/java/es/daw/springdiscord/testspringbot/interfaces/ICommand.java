package es.daw.springdiscord.testspringbot.interfaces;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface ICommand {
    String getName();
    void execute(SlashCommandInteractionEvent event);
}
