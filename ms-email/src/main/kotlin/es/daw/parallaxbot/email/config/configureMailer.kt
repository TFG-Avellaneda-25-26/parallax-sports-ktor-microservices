package es.daw.parallaxbot.email.config

import com.google.api.client.http.apache.v2.ApacheHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail

/**
 * Creates a Gmail API client used by the email notification service.
 *
 * @return Gmail client configured with transport and JSON factory.
 */
fun configureMailer(): Gmail {
    return Gmail.Builder(
        ApacheHttpTransport(),
        GsonFactory.getDefaultInstance(),
        null
    ).setApplicationName("ParallaxBot").build()
}