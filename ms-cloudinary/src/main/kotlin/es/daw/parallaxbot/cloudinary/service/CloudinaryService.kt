package es.daw.parallaxbot.cloudinary.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

/**
 * Manages Cloudinary artifact upload and lookup for event screenshot storage.
 */
class CloudinaryService(private val cloudinary: Cloudinary) {

    private val logger = LoggerFactory.getLogger(CloudinaryService::class.java)

    /**
     * Uploads an event image and returns its secure URL.
     *
     * @param fileBytes image payload bytes.
     * @param eventId stable event identifier used as Cloudinary public ID.
     * @return secure asset URL when upload succeeds; null otherwise.
     */
    suspend fun uploadImage(
        fileBytes: ByteArray,
        eventId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val options = ObjectUtils.asMap(
                "public_id", eventId,
                "folder", "parallaxbot/events",
                "resource_type", "image",
                "overwrite", true
            )

            val uploadResult = cloudinary.uploader().upload(fileBytes, options)
            uploadResult["secure_url"] as String
        } catch (e: Exception) {
            logger.error("Error while uploading image: ${e.message}")
            null
        }
    }

    /**
     * Retrieves an existing uploaded image URL for the given event.
     *
     * @param eventId event identifier used in Cloudinary public ID path.
     * @return secure URL when asset exists; null when missing or lookup fails.
     */
    suspend fun getExistingUrl(eventId: String): String? = withContext(Dispatchers.IO) {
        try {
            val resource = cloudinary.api().resource("parallaxbot/events/$eventId", ObjectUtils.emptyMap())
            resource["secure_url"] as String
        } catch (e: Exception) {
            null
        }
    }
}
