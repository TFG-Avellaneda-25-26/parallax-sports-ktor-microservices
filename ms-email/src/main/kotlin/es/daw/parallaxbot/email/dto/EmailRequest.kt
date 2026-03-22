package es.daw.parallaxbot.email.dto

import es.daw.parallaxbot.common.dto.EventDTO
import kotlinx.serialization.Serializable

/**
 * Request payload used by the email notification endpoint.
 */
@Serializable
data class EmailRequest(
    val to: String,
    val event: EventDTO,
    val imageUrl: String,
    val accessToken: String
)