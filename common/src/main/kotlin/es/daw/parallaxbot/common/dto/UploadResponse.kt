package es.daw.parallaxbot.common.dto

data class UploadResponse(
    val url: String,
    val publicId: String,
    val format: String? = null,
    val bytes: Long? = null,
)