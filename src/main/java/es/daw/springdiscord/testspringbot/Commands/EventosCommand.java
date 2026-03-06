package es.daw.springdiscord.testspringbot.Commands;

import es.daw.springdiscord.testspringbot.dto.EventoDTO;
import es.daw.springdiscord.testspringbot.interfaces.ICommand;
import es.daw.springdiscord.testspringbot.services.DiscordBotService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventosCommand implements ICommand {

    @Autowired
    @Lazy
    private DiscordBotService discordBotService;

    @Override
    public String getName() {
        return "eventos";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {

        try {
            OptionMapping tipoOption = event.getOption("tipo");
            String tipoBusqueda = (tipoOption != null) ? tipoOption.getAsString() : null;

            List<EventoDTO> eventos = discordBotService.obtenerEventos(tipoBusqueda);

            if (eventos.isEmpty()) {
                String mensajeError = (tipoBusqueda != null)
                        ? "No se encontraron eventos del tipo: **" + tipoBusqueda + "**"
                        : "No hay eventos programados en este momento.";

                event.getHook().sendMessage(mensajeError).queue();
            }


            Map<String, List<EventoDTO>> eventosAgrupados = eventos.stream()
                    .collect(Collectors.groupingBy(e -> e.getTipoEvento().toUpperCase()));

            eventosAgrupados.forEach((tipo, lista) -> {

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("📅 CARTELERA DE LEAGUE OF LEGENDS");
                embed.setColor(Color.BLACK); // Color neutro ya que hay varios tipos
                embed.setThumbnail("https://imgs.search.brave.com/c7rQ3qedkq1hxKS1NWqQEI_Bip4nCqjaDa9lAamEt7U/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9zdHls/ZXMucmVkZGl0bWVk/aWEuY29tL3Q1XzJy/Znh4L3N0eWxlcy9j/b21tdW5pdHlJY29u/Xzl5ajY2Y2pmOG9x/NjEucG5n"); // Icono opcional de LoL
                embed.setFooter("Consulta actualizada");

                embed.addField("▬▬▬▬▬▬▬ " + tipo + " ▬▬▬▬▬▬▬", "", false);

                for (EventoDTO evento : lista) {

                    Color colorEvento = switch (evento.getTipoEvento().toUpperCase()) {
                        case "LEC" -> Color.ORANGE;
                        case "LVP" -> Color.BLUE;
                        case "WORLDS" -> Color.YELLOW;
                        case "MSI" -> Color.MAGENTA;
                        default -> Color.GREEN;
                    };

                    embed.setColor(colorEvento);
                    embed.setTitle("🏆 " + evento.getNombre());

                    long unixTime = evento.getFechaHora().atZone(ZoneId.systemDefault()).toEpochSecond();

                    embed.addField("⚔️ Enfrentamiento", "**" + evento.getEquipoLocal() + "** vs **" + evento.getEquipoVisitante() + "**", false);
                    embed.addField("📅 Fecha", "<t:" + unixTime + ":F>", true);
                    embed.addField("📍 Ubicación", evento.getUbicacion(), true);

                    embed.setFooter("Categoría: " + evento.getTipoEvento());
                }

                embed.addBlankField(false);
                event.getHook().sendMessageEmbeds(embed.build()).queue();
            });


        } catch (Exception e) {
            event.getHook().sendMessage("ERROR al obtener datos: " + e.getMessage()).queue();
            e.printStackTrace();
        }
    }

}
