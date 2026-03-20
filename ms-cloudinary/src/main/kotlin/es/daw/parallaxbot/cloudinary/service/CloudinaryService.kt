package es.daw.parallaxbot.cloudinary.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

class CloudinaryService(private val cloudinary: Cloudinary) {

    val logger = LoggerFactory.getLogger(CloudinaryService::class.java)

    suspend fun uploadImage(
        fileBytes: ByteArray,
        fileName: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val options = ObjectUtils.asMap(
                "public_id", fileName,
                "folder", "parallaxbot/events",
                "resource_type", "image"
            )

            val uploadResult = cloudinary.uploader().upload(fileBytes, options)

            uploadResult["secure_url"] as String
        } catch (e: Exception) {
            logger.error("Error while uploading image: ${e.message}")
            ""
        }

    }

    suspend fun getExistingUrl(publicId: String): String? = withContext(Dispatchers.IO) {
        try {
            val resource = cloudinary.api().resource("parallaxbot/events/$publicId", ObjectUtils.emptyMap())

            resource["secure_url"] as String
        } catch (e: Exception) {
            logger.error("Error while loading image: ${e.message}")
            null
        }
    }
}
