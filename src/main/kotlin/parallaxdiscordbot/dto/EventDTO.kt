package es.daw.parallaxdiscordbot.dto

import es.daw.parallaxdiscordbot.utils.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class EventDTO(
    val id: Long? = null,
    val eventName: String,
    val localTeam: String,
    val visitorTeam: String,
    val eventType: String,
    val location: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateTime: LocalDateTime
    )