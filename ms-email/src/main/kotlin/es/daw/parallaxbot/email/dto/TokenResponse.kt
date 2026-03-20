package es.daw.parallaxbot.email.dto

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val accessToken: String,
    val expiresIn: Long,
)