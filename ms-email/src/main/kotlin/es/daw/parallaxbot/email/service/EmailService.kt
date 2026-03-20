package es.daw.parallaxbot.email.service

import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import es.daw.parallaxbot.common.dto.EventDTO
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.Properties

class EmailService(
    private val gmail: Gmail,
) {

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

    private fun createMimeMessage(to: String, event: EventDTO, imageUrl: String): MimeMessage {
        val session = Session.getDefaultInstance(Properties())
        return MimeMessage(session).apply {
            setFrom(InternetAddress("no-reply@parallaxbot.es"))
            addRecipient(jakarta.mail.Message.RecipientType.TO, InternetAddress(to))
            subject = "Event Alert: ${event.eventName}"

            val htmlBody = """
                <div style="font-family: sans-serif; border: 1px solid #ddd; padding: 20px; border-radius: 8px;">
                    <h2 style="color: #1a73e8;">Notificación de ParallaxBot</h2>
                    <p>Se ha detectado el evento: <strong>${event.eventName}</strong></p>
                    <img src="$imageUrl" style="width: 100%; max-width: 500px; border-radius: 4px; margin: 10px 0;">
                    <p>Fecha y hora: ${event.dateTime}</p>
                    <a href="" style="color: #1a73e8;">Ver más detalles</a>
                </div>
            """.trimIndent()

            setContent(htmlBody, "text/html; charset=utf-8")
        }
    }
}