package es.daw.parallaxbot.cloudinary.config

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import es.daw.parallaxbot.common.config.CloudinaryConfig

/**
 * Builds the Cloudinary SDK client using configured credentials.
 */
// -> Source: Service Startup || Action: Create Cloudinary SDK singleton || Strategy: secure transport enabled for all provider requests
// -> API: Cloudinary REST API || Auth: API Key + Secret || Scope: image upload and resource lookup
fun configureCloudinary(config: CloudinaryConfig) : Cloudinary {
    return Cloudinary(ObjectUtils.asMap(
        "cloud_name", config.cloudName,
        "api_key", config.apiKey,
        "api_secret", config.apiSecret,
        "secure", true
    ))
}