package es.daw.parallaxbot.email.config

import es.daw.parallaxbot.common.config.EmailConfig
import es.daw.parallaxbot.email.service.EmailService
import es.daw.parallaxbot.email.service.GoogleTokenManager
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory


/**
 * Registers OAuth and internal verification routes for the email microservice.
 */
// -> Triggers: Ktor route registration at service startup || Contract: exposes Gmail OAuth and verification endpoints
fun Application.configureRouting() {

    val logger = LoggerFactory.getLogger(this::class.java)
    val googleTokenManager by inject<GoogleTokenManager>()
    val emailConfig by inject<EmailConfig>()
    val redirectUri = emailConfig.oauthRedirectUri
    val emailService by inject<EmailService>()

    routing {
        // -> Triggers: GET /auth/google/login || Contract: redirects to Google OAuth consent URL
        get("/auth/google/login") {

            val authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                    "client_id=${emailConfig.clientId}&" +
                    "redirect_uri=$redirectUri&" +
                    "response_type=code&" +
                    "scope=https://www.googleapis.com/auth/gmail.send&" +
                    "access_type=offline&" +
                    "prompt=consent"

            logger.info("Login URL: $authUrl")
            call.respondRedirect(authUrl)
        }

        // -> Triggers: GET /auth/callback || Contract: stores refresh token and returns success/error text response
        get("auth/callback") {
           val code = call.parameters["code"] ?: return@get call.respondText("Code missing", status = HttpStatusCode.BadRequest)

            try {
                googleTokenManager.initialExchange(code, redirectUri)
                call.respondText("Success Refresh token saved in redis", status = HttpStatusCode.OK)
            } catch(e: Exception) {
                logger.error("Error during callback ${e.message}")
                call.respondText("Error while getting callback", status = HttpStatusCode.InternalServerError)
            }
        }

        // -> Triggers: POST /internal/email/verify || Contract: sends verification email and returns status payload (200/500)
        post("/internal/email/verify") {
            val request = call.receive<VerificationRequest>()

            try {
                val token = googleTokenManager.getAccessToken()

                emailService.sendVerificationEmail(
                    to = request.email,
                    verificationCode = request.verificationCode,
                    token = token
                )

                call.respond(HttpStatusCode.OK, mapOf("message" to "Verification email sent to ${request.email}"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Email verification failed: ${e.message}")
            }
        }
    }
}