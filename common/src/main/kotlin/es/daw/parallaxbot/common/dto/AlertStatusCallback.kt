package es.daw.parallaxbot.common.dto

import kotlinx.serialization.Serializable

@Serializable
class AlertStatusCallback(
    val status: String,
    val workerId: String,
    val streamMessageId: String? = null,
    val providerMessageId: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val httpStatus: Int? = null,
    val latencyMs: Long? = null
    )