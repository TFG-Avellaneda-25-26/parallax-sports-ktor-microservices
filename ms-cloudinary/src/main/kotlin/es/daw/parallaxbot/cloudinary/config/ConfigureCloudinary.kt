package es.daw.parallaxbot.cloudinary.config

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import es.daw.parallaxbot.common.config.CloudinaryConfig

/**
 * Builds a configured Cloudinary client instance from application credentials.
 *
 * @param config Cloudinary tenant credentials.
 * @return initialized Cloudinary client configured for secure URLs.
 */
fun configureCloudinary(config: CloudinaryConfig) : Cloudinary {
    return Cloudinary(ObjectUtils.asMap(
        "cloud_name", config.cloudName,
        "api_key", config.apiKey,
        "api_secret", config.apiSecret,
        "secure", true
    ))
}