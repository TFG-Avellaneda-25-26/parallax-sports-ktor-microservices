package es.daw.springdiscord.testspringbot.config;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscordInitializer {

    private final JDA jda;

    @Value("${discord.server.id}")
    private String serverId;

    @EventListener(ContextRefreshedEvent.class)
    public void setupCommands() {
        try {
            jda.awaitReady();

            OptionData ligaOption = new OptionData(OptionType.STRING, "tipo", "Selecciona la liga", false)
                    .addChoice("LEC - Europa", "LEC")
                    .addChoice("LVP - Superliga", "LVP")
                    .addChoice("Worlds - Mundial", "WORLDS")
                    .addChoice("MSI - Invitational", "MSI");

            jda.getGuildById(serverId).updateCommands().addCommands(
                    Commands.slash("eventos", "Lista los próximos eventos de League of Legends")
                            .addOptions(ligaOption)
            ).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
