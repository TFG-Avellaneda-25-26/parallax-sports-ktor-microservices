package es.daw.parallaxbot.email.service

import es.daw.parallaxbot.common.dto.AlertStreamMessage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonObject
import org.slf4j.LoggerFactory
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.util.Base64

class EmailService(
    private val httpClient: HttpClient,
    private val templateEngine: TemplateEngine
) {

    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    suspend fun sendEvent(message: AlertStreamMessage, accessToken: String): String? {
        return try {
            val context = Context().apply { setVariable("event", message) }
            val htmlContent = templateEngine.process("event", context)
            val to = "manueldl12062003@gmail.com"

            val googleMessageId = sendGmail(to, "Parallax event notification", htmlContent, accessToken)

            logger.info("Parallax event notification sent")
            googleMessageId
        } catch (e: Exception) {
            logger.error("Error while sending event: ${e.message}")
            null
        }
    }

    suspend fun sendVerificationEmail(to: String, verificationCode: String, token: String) {
        try {
            val context = Context().apply { setVariable("code", verificationCode) }
            val htmlContent = templateEngine.process("verification", context)
            sendGmail(to, "Parallax Account verification", htmlContent, token)
            logger.info("Verification email sent")
        } catch (e: Exception) {
            logger.error("Error while sending verification email ${e.message}")
        }
    }

    private suspend fun sendGmail(to: String, subject: String, htmlBody: String, token: String): String {
        val headers = listOf(
            "from: me",
            "To: $to",
            "Subject: $subject",
            "MIME-Version: 1.0",
            "Content-Type: text/html; charset=UTF-8"
        ).joinToString("\r\n")

        val rawMessage = "$headers\r\n\r\n$htmlBody"

        val encodedEmail = Base64.getUrlEncoder().withoutPadding().encodeToString(rawMessage.toByteArray(Charsets.UTF_8))

        val response = httpClient.post("https://gmail.googleapis.com/gmail/v1/users/me/messages/send") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(mapOf("raw" to encodedEmail))
        }

        logger.info("Google Response: ${response.status}")

        return if (response.status.isSuccess()) {
            val body = response.body<JsonObject>()
            body["id"]?.toString()?.replace("\"", "") ?: "unknown-id"
        } else {
            throw Exception("Google API error: ${response.status}")
        }
    }
}