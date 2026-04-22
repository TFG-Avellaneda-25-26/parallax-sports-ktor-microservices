package es.daw.parallaxbot.cloudinary.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

/**
 * Provider adapter for Cloudinary upload and resource lookup operations.
 */
class CloudinaryService(private val cloudinary: Cloudinary) {

    private val logger = LoggerFactory.getLogger(CloudinaryService::class.java)
    private val folder = "parallaxbot/events"

    suspend fun uploadImage(
        fileBytes: ByteArray,
        eventId: String,
        hash: String,
    ): String? = withContext(Dispatchers.IO) {
        try {
            val publicId = "${eventId}_${hash}"
            val options = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", "image",
                "overwrite", false,
            )
            val uploadResult = cloudinary.uploader().upload(fileBytes, options)
            uploadResult["secure_url"] as String
        } catch (e: Exception) {
            logger.error("Error while uploading image: ${e.message}")
            null
        }
    }

    suspend fun getExistingUrl(eventId: String, hash: String): String? = withContext(Dispatchers.IO) {
        try {
            val publicId = "${eventId}_${hash}"
            val resource = cloudinary.api().resource("$folder/$publicId", ObjectUtils.emptyMap())
            resource["secure_url"] as String
        } catch (e: Exception) {
            null
        }
    }
}
