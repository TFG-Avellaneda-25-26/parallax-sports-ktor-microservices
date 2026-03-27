package es.daw.parallaxbot.email.config

import kotlinx.serialization.Serializable

@Serializable
data class VerificationRequest(
    val email: String,
    val verificationCode: String
)