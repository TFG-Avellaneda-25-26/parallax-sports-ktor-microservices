package es.daw.parallaxbot.cloudinary.config

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import es.daw.parallaxbot.common.config.CloudinaryConfig

fun configureCloudinary(config: CloudinaryConfig) : Cloudinary {
    return Cloudinary(ObjectUtils.asMap(
        "cloud_name", config.cloudName,
        "api_key", config.apiKey,
        "api_secret", config.apiSecret,
        "secure", true
    ))
}