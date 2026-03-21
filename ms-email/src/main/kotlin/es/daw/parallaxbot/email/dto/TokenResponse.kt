package es.daw.parallaxbot.email.dto

import kotlinx.serialization.Serializable

/**
 * OAuth token response contract used by email sender flows.
 */
@Serializable
data class TokenResponse(
    val accessToken: String,
    val expiresIn: Long,
)