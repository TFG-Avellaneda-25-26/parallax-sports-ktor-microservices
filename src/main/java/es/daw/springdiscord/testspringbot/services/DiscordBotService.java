package es.daw.springdiscord.testspringbot.services;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ScreenshotType;
import es.daw.springdiscord.testspringbot.dto.EventoDTO;
import jakarta.annotation.PostConstruct;
import kotlin.collections.ArrayDeque;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscordBotService {

    private final List<EventoDTO> eventosMemoria = Collections.synchronizedList(new ArrayDeque<>());

    @Value("${catalog.api-url}")
    private String URL;

    @Value("${discord.channel.ping.id}")
    String discordChannel;

    private final WebClient webClient;
    private final TemplateEngine templateEngine;

    private Playwright playwright;
    private Browser browser;

    @Lazy
    private final JDA jda;

    @EventListener(ApplicationReadyEvent.class)
    public void cargarEventos() {
        try {
            System.out.println("Bot iniciado: Cargando eventos en memoria...");
            List<EventoDTO> eventos = webClient.get()
                    .uri(URL + "/prox")
                    .retrieve()
                    .bodyToFlux(EventoDTO.class)
                    .collectList()
                    .block();

            Optional.ofNullable(eventos)
                    .filter(list -> !list.isEmpty())
                    .ifPresentOrElse(
                            list-> {
                                eventosMemoria.clear();
                                eventosMemoria.addAll(list);
                                System.out.println("Eventos Cargados correctamente");
                            },
                            () -> System.out.println("No hay eventos en este momento")
                    );
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Scheduled(fixedRate = 60000)
    public void procesarNotificaciones() {

        System.out.println("Verificando eventos proximos en memoria...");
        Optional.of(eventosMemoria)
                .filter(List::isEmpty)
                .ifPresent(
                        list -> {
                            System.out.println("Memoria vacia. Reintando carga...");
                            cargarEventos();
                        }
                );

        if (jda.getStatus() != JDA.Status.CONNECTED) return;

        System.out.println("eventos: " + eventosMemoria.toString());
        LocalDateTime targetTime = LocalDateTime.now().plusMinutes(30).truncatedTo(ChronoUnit.MINUTES);

        eventosMemoria.stream()
                .filter(e -> {
                    long diferencia = ChronoUnit.MINUTES.between(LocalDateTime.now(), e.getFechaHora());
                    return diferencia == 30;
                })
                .forEach(this::enviarAlertaDiscord);
    }

    public void enviarAlertaDiscord(EventoDTO evento) {
        System.out.println("Intentando enviar las alertas...");

        TextChannel canal = jda.getTextChannelById(discordChannel);
        if (canal == null) return;

        byte[] imagenByte = generarImagen(evento);
        if (imagenByte == null) return;

        long unixTime = evento.getFechaHora().atZone(ZoneId.systemDefault()).toEpochSecond();
        String mensajeTexto = String.format("!@everyone! **%s** empieza <t:%d:R>\"", evento.getNombre(), unixTime);

        canal.sendMessage(mensajeTexto)
                .addFiles(net.dv8tion.jda.api.utils.FileUpload.fromData(imagenByte, "evento.png"))
                .queue(
                        success -> System.out.println("Notificacion enviada con imagen para: " + evento.getNombre()),
                        error -> System.out.println("Error al enviar mensaje a Discord: " + error.getMessage())
                );
    }

    public List<EventoDTO> obtenerEventos(String tipo) {

        Optional.of(eventosMemoria)
                .filter(List::isEmpty)
                .ifPresent(
                        list -> {
                            System.out.println("Memoria vacia. Reintando carga...");
                            cargarEventos();
                        }
                );

        if (tipo == null) {
            return eventosMemoria;
        }

        return eventosMemoria.stream()
                .filter(e -> e.getTipoEvento().equalsIgnoreCase(tipo))
                .collect(Collectors.toList());
    }

    public byte[] generarImagen(EventoDTO evento) {
        System.out.println("Generando imagen para evento: " + evento.getNombre());

        Context context = new Context();
        context.setVariable("evento", evento);
        String htmlProcesado = templateEngine.process("tarjeta-evento", context);

        try (BrowserContext browserContext = browser.newContext(new Browser.NewContextOptions().setViewportSize(800, 400))) {
            Page page = browserContext.newPage();
            page.setContent(htmlProcesado);

            return page.screenshot(new Page.ScreenshotOptions().setType(ScreenshotType.PNG));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    @PostConstruct
    public void initPlaywright() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        System.out.println("Motor Playwright listo");
    }
}
