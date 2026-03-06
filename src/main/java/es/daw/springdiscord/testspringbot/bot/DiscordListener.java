package es.daw.springdiscord.testspringbot.bot;

import es.daw.springdiscord.testspringbot.interfaces.ICommand;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DiscordListener extends ListenerAdapter {

    private final Map<String, ICommand> comandos = new HashMap<>();

    @Autowired
    public void setComandos(List<ICommand> listaComandos) {
        for (ICommand cmd : listaComandos) {
            comandos.put(cmd.getName(), cmd);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent event) {

        ICommand comando = comandos.get(event.getName());

        if (comando != null) {
            event.deferReply().queue();
            comando.execute(event);
        }


    }
}
