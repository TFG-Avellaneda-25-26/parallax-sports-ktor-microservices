package es.daw.parallaxbot.email.service

import es.daw.parallaxbot.common.config.EmailConfig
import es.daw.parallaxbot.email.config.GoogleTokenResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Parameters
import io.lettuce.core.RedisClient
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes

class GoogleTokenManager(
    redisClient: RedisClient,
    private val httpClient: HttpClient,
    private val emailConfig: EmailConfig
) {
    private val logger = LoggerFactory.getLogger(GoogleTokenManager::class.java)

    private val ACCESS_TOKEN_KEY = "auth:google:access_token"
    private val REFRESH_TOKEN_KEY = "auth:google:refresh_token"

    private val sync = redisClient.connect().sync()

    suspend fun getAccessToken(): String {
        val cachedToken = sync.get(ACCESS_TOKEN_KEY)
        if (cachedToken != null) return cachedToken

        val refreshToken = sync.get(REFRESH_TOKEN_KEY)
            ?: throw IllegalStateException("No access token available")

        return refreshAccessToken(refreshToken)
    }

    private suspend fun refreshAccessToken(refreshToken: String): String {
        logger.info("Refreshing access token")

        val response = httpClient.post("https://oauth2.googleapis.com/token") {
            setBody(FormDataContent(Parameters.build {
                append("client_id", emailConfig.clientId)
                append("client_secret", emailConfig.clientSecret)
                append("refresh_token", refreshToken)
                append("grant_type", "refresh_token")
            }))
        }.body<GoogleTokenResponse>()

        sync.setex(ACCESS_TOKEN_KEY, 55.minutes.inWholeSeconds, response.accessToken)

        return response.accessToken
    }

    suspend fun initialExchange(code: String, redirectUri: String) {
        logger.info("Initial exchange for token")

        val response = httpClient.post("https://oauth2.googleapis.com/token") {
            setBody(FormDataContent(Parameters.build {
                append("client_id", emailConfig.clientId)
                append("client_secret", emailConfig.clientSecret)
                append("code", code)
                append("redirect_uri", redirectUri)
                append("grant_type", "authorization_code")
            }))
        }.body<GoogleTokenResponse>()

        if (response.refreshToken != null) {
            sync.set(REFRESH_TOKEN_KEY, response.refreshToken)
            logger.info("Refresh token saved")
        } else {
            logger.warn("Refresh token not saved")
        }

        sync.setex(ACCESS_TOKEN_KEY, 55.minutes.inWholeSeconds, response.accessToken)
    }
}