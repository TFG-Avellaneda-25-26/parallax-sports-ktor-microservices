package es.daw.parallaxbot.common.dto

import kotlinx.serialization.Serializable

/**
 * Event payload contract shared across notification services.
 */
@Serializable
data class EventDTO(
    val id: Long,
    val name: String,
    val eventType: String,
    val status: String,
    val startTimeUtc: String,
    val endTimeUtc: String? = null,
    val competition: CompetitionDTO? = null,
    val venue: VenueDTO? = null,
    val metadata: Map<String, String>? = emptyMap()
    )

/**
 * Competition metadata attached to an event.
 */
@Serializable
data class CompetitionDTO(
    val id: Long? = null,
    val name: String? = null,
    val category: String? = null
)

/**
 * Venue metadata attached to an event.
 */
@Serializable
data class VenueDTO(
    val id: Long? = null,
    val name: String? = null,
    val city: String? = null,
    val timezone: String? = null
)

