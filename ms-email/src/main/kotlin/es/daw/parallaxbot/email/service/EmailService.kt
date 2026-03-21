package es.daw.parallaxbot.email.service

import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import es.daw.parallaxbot.common.dto.AlertStreamMessage
import es.daw.parallaxbot.common.dto.EventDTO
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.Properties

/**
 * Delivers notification emails through the Gmail API.
 */
class EmailService(
    private val gmail: Gmail,
) {

        /*============================================================
            PUBLIC CONTRACT
            Outbound email operations exposed to callers
        ============================================================*/

    /**
         * Submits a notification email to Gmail using the provided OAuth access token.
     *
     * @param to recipient mailbox address.
         * @param event event payload associated with this notification flow.
         * @param imageUrl externally hosted image URL embedded in the HTML body.
     * @param accessToken Gmail OAuth token scoped for send permissions.
         * @return Unit after Gmail accepts the send request.
     */

    suspend fun sendEventEmail(
        to: String,
        event: EventDTO,
        imageUrl: String,
        accessToken: String
    ) = withContext(Dispatchers.IO) {

        val emailContent = createMimeMessage(to, event, imageUrl)

        val buffer = ByteArrayOutputStream()
        emailContent.writeTo(buffer)

        val encodedEmail = Base64.getUrlEncoder().encodeToString(buffer.toByteArray())
        val message = Message().setRaw(encodedEmail)

        gmail.users().messages().send("me", message)
            .setAccessToken(accessToken)
            .execute()
    }

        /*============================================================
            MESSAGE COMPOSITION
            MIME payload construction for Gmail submission
        ============================================================*/

    /**
         * Builds the HTML MIME message payload for one notification email.
     *
     * @param to recipient mailbox address.
         * @param event event payload reserved for template enrichment in this flow.
         * @param imageUrl event artifact URL rendered in the message body.
     * @return MIME message ready for Gmail API submission.
     */
    private fun createMimeMessage(to: String, event: EventDTO, imageUrl: String): MimeMessage {
        val session = Session.getDefaultInstance(Properties())
        return MimeMessage(session).apply {
            setFrom(InternetAddress("no-reply@parallaxbot.es"))
            addRecipient(jakarta.mail.Message.RecipientType.TO, InternetAddress(to))
            subject = "Event Alert: "

            val htmlBody = """
                <div style="font-family: sans-serif; border: 1px solid #ddd; padding: 20px; border-radius: 8px;">
                    <h2 style="color: #1a73e8;">Notificación de ParallaxBot</h2>
                    <img src="$imageUrl" style="width: 100%; max-width: 500px; border-radius: 4px; margin: 10px 0;">
                    <a href="" style="color: #1a73e8;">Ver más detalles</a>
                </div>
            """.trimIndent()

            setContent(htmlBody, "text/html; charset=utf-8")
        }
    }

    /**
     * Placeholder stream entry point for Redis-driven email delivery.
     *
     * Current implementation is a no-op and exists only to keep the worker contract stable
     * while email template mapping is completed.
     *
     * @param message alert stream event to be transformed into an email payload.
     * @param artifactUrl optional artifact URL to embed in the notification.
     * @return Unit.
     */
    fun sendEvent(message: AlertStreamMessage, artifactUrl: String?) {}
}