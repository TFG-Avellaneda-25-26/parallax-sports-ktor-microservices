package es.daw.springdiscord.testspringbot.config;

import es.daw.springdiscord.testspringbot.bot.DiscordListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class DiscordConfig {
    @Value("${discord.token}")
    private String token;

    @Bean
    public JDA jda(@Lazy DiscordListener discordListener) throws InterruptedException {
        JDA jda = JDABuilder.createDefault(token)
                // Habilitamos el intento para leer el contenido de los mensajes
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                // Registramos nuestro listener
                .addEventListeners(discordListener)
                .build();

        return jda;
    }
}
