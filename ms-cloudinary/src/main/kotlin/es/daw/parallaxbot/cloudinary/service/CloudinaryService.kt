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

        /*============================================================
            CLOUDINARY WRITE OPERATIONS
            Persist event artifacts and return canonical secure URL
        ============================================================*/
        // -> Source: Internal Upload Request || Action: Upload image bytes to Cloudinary folder || Strategy: IO dispatcher execution, return null on provider errors
        // -> API: Cloudinary Upload API || Auth: API Key + Secret || Scope: image write/overwrite in parallaxbot/events
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

    /*============================================================
      CLOUDINARY READ OPERATIONS
      Resolve existing artifact URL for idempotent screenshot flow
    ============================================================*/
    // -> Source: Internal Lookup Request || Action: Fetch existing Cloudinary resource URL || Strategy: IO dispatcher execution, return null when resource is absent or call fails
    // -> API: Cloudinary Admin Resource API || Auth: API Key + Secret || Scope: image metadata lookup in parallaxbot/events
    suspend fun getExistingUrl(eventId: String): String? = withContext(Dispatchers.IO) {
        try {
            val resource = cloudinary.api().resource("parallaxbot/events/$eventId", ObjectUtils.emptyMap())
            resource["secure_url"] as String
        } catch (e: Exception) {
            null
        }
    }
}
