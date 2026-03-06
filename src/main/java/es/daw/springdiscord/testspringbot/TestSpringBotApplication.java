package es.daw.springdiscord.testspringbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TestSpringBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestSpringBotApplication.class, args);
    }

}
