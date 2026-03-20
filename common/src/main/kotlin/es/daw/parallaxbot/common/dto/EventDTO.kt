package es.daw.parallaxbot.common.dto

import es.daw.parallaxbot.common.LocalDateTimeSerializer
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