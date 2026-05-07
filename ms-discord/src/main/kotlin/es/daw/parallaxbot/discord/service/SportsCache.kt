package es.daw.parallaxbot.discord.service

import es.daw.parallaxbot.common.dto.SportDTO
import es.daw.parallaxbot.discord.client.SpringDiscordAdminClient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

class SportsCache(
    private val adminClient: SpringDiscordAdminClient
) {
    private val logger = LoggerFactory.getLogger(SportsCache::class.java)
    private val mutex = Mutex()
    private val ttl = Duration.ofMinutes(10)

    @Volatile
    private var cachedSports: List<SportDTO> = emptyList()

    @Volatile
    private var lastFetch: Instant? = null

    suspend fun search(query: String, limit: Int = 25): List<SportDTO> {
        val sports = getSports()
        if (sports.isEmpty()) return emptyList()

        val needle = query.trim()
        val filtered = if (needle.isBlank()) {
            sports
        } else {
            sports.filter {
                it.name.contains(needle, ignoreCase = true) || it.key.contains(needle, ignoreCase = true)
            }
        }

        return filtered.take(limit)
    }

    private suspend fun getSports(): List<SportDTO> {
        val now = Instant.now()
        if (isFresh(now)) return cachedSports

        return mutex.withLock {
            val lockedNow = Instant.now()
            if (isFresh(lockedNow)) return cachedSports

            val fetched = adminClient.getSports()
            if (fetched.isNotEmpty()) {
                cachedSports = fetched
                lastFetch = lockedNow
            } else if (lastFetch == null) {
                cachedSports = emptyList()
            } else {
                logger.warn(
                    "Sports fetch returned empty list; keeping cached list size={}",
                    cachedSports.size
                )
            }

            cachedSports
        }
    }

    private fun isFresh(now: Instant): Boolean {
        val fetchedAt = lastFetch ?: return false
        return cachedSports.isNotEmpty() && Duration.between(fetchedAt, now) < ttl
    }
}
