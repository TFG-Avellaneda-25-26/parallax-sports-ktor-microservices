package es.daw.springdiscord.testspringbot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventoDTO {
    private Long id;
    private String nombre;
    private String tipoEvento;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaHora;

    private String ubicacion;
    private String equipoLocal;
    private String equipoVisitante;
    private String descripcion;
}
