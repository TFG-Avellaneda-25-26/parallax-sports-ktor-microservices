package es.daw.parallaxbot.common.dto

import kotlinx.serialization.Serializable

@Serializable
data class SportDTO(
    val key: String,
    val name: String
)