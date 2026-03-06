package es.daw.springdiscord.testspringbot.services;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramBotService extends TelegramLongPollingBot {

    @Value("${telegram.username}")
    private String botUsername;

    @Value("${telegram.token}")
    private String botToken;

    @Value("${telegram.channel}")
    private Long telegramChannel;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String mensajeRecibido = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (mensajeRecibido.equalsIgnoreCase("/hola")) {
                enviarTexto(chatId, "¡Hola! Soy tu bot de eSports gestionado con Spring Boot 🚀");
            }
        }
    }

    public void enviarNotificacionesGrupal(String texto) {
        enviarTexto(telegramChannel, texto);
    }

    private void enviarTexto(Long chatId, String texto) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(texto)
                .parseMode("Markdown")
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
