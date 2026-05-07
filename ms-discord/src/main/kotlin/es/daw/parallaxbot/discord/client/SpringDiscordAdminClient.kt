package es.daw.parallaxbot.discord.client

import es.daw.parallaxbot.common.config.DiscordConfig
import es.daw.parallaxbot.common.dto.SportDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import kotlin.math.log

/**
 * Thin HTTP client for Spring's /api/internal/discord admin endpoints.
 *
 * All write operations send the shared {@code X-Api-Key} header and return
 * whether the request was accepted.
 */
class SpringDiscordAdminClient(
    private val httpClient: HttpClient,
    private val discordConfig: DiscordConfig
) {
    private val logger = LoggerFactory.getLogger(SpringDiscordAdminClient::class.java)
    private val baseUrl: String = discordConfig.discordAdminApiUrl

    @Serializable
    data class GuildInstallRequest(val ownerDiscordId: String?, val installedAt: String)

    @Serializable
    data class GuildChannelRequest(
        val channelId: String,
        val sportKey: String? = null,
        val setByDiscordUserId: String? = null
    )

    @Serializable
    data class DeliveryRequest(val mode: String, val guildId: String? = null)

    @Serializable
    data class UserIdResponse(val userId: Long)

    suspend fun resolveUserIdByDiscord(discordUserId: String): Long? {
        return try {
            val response = httpClient.get("$baseUrl/users/by-discord/$discordUserId") {
                applyHeaders()
            }
            if (response.status.isSuccess()) response.body<UserIdResponse>().userId else null
        } catch (e: Exception) {
            logger.warn("Failed to resolve user id for discord $discordUserId: ${e.message}")
            null
        }
    }

    suspend fun installGuild(guildId: String, ownerDiscordId: String?, installedAtIso: String): Boolean =
        sendAccepted {
            httpClient.post("$baseUrl/guilds/$guildId/install") {
                applyHeaders()
                setBody(GuildInstallRequest(ownerDiscordId, installedAtIso))
            }
        }

    suspend fun uninstallGuild(guildId: String): Boolean =
        sendAccepted {
            httpClient.delete("$baseUrl/guilds/$guildId") { applyHeaders() }
        }

    suspend fun upsertChannel(guildId: String, channelId: String, sportKey: String?, setByDiscordUserId: String?): Boolean =
        sendAccepted {
            httpClient.post("$baseUrl/guilds/$guildId/channel") {
                applyHeaders()
                setBody(GuildChannelRequest(channelId, sportKey, setByDiscordUserId))
            }
        }

    suspend fun upsertUserDelivery(userId: Long, mode: String, guildId: String?): Boolean =
        sendAccepted {
            httpClient.put("$baseUrl/users/$userId/delivery") {
                applyHeaders()
                setBody(DeliveryRequest(mode, guildId))
            }
        }

    suspend fun upsertUserSportDelivery(userId: Long, sportId: Long, mode: String, guildId: String?): Boolean =
        sendAccepted {
            httpClient.put("$baseUrl/users/$userId/delivery/sports/$sportId") {
                applyHeaders()
                setBody(DeliveryRequest(mode, guildId))
            }
        }

    suspend fun deleteUserSportDelivery(userId: Long, sportId: Long): Boolean =
        sendAccepted {
            httpClient.delete("$baseUrl/users/$userId/delivery/sports/$sportId") { applyHeaders() }
        }

    suspend fun upsertUserSportDeliveryByKey(userId: Long, sportKey: String, mode: String, guildId: String?): Boolean =
        sendAccepted {
            httpClient.put("$baseUrl/users/$userId/delivery/sport-keys/$sportKey") {
                applyHeaders()
                setBody(DeliveryRequest(mode, guildId))
            }
        }

    suspend fun deleteUserSportDeliveryByKey(userId: Long, sportKey: String): Boolean =
        sendAccepted {
            httpClient.delete("$baseUrl/users/$userId/delivery/sport-keys/$sportKey") { applyHeaders() }
        }

    private fun io.ktor.client.request.HttpRequestBuilder.applyHeaders() {
        contentType(ContentType.Application.Json)
        header("X-Api-Key", discordConfig.apiKey)
    }

    private suspend fun sendAccepted(block: suspend () -> io.ktor.client.statement.HttpResponse): Boolean {
        return try {
            val response = block()
            val ok = response.status.isSuccess()
            if (!ok) {
                logger.warn("Discord admin call failed status={}", response.status)
            }
            ok
        } catch (e: Exception) {
            logger.error("Discord admin call error: ${e.message}")
            false
        }
    }

    suspend fun getSports(): List<SportDTO> {
        return try {
            val response = httpClient.get("$baseUrl/sports")

            if (response.status.isSuccess()) {
                val sports = response.body<List<SportDTO>>()
                logger.info("Sports retrieved successfully, $sports")
                sports
            } else emptyList()
        } catch (e: Exception) {
            logger.warn("Failed to fetch sports: ${e.message}")
            emptyList()
        }
    }
}
