package es.daw.parallaxdiscordbot.services

import es.daw.parallaxdiscordbot.dto.EventDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.util.Collections


class EventService(
    private val httpclient: HttpClient,
    private val apiUrl: String
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val eventCache: MutableList<EventDTO> = Collections.synchronizedList(mutableListOf())

    suspend fun fetchEvents(): List<EventDTO> {
        try {
            logger.info("Fetching events from API...")

            val events: List<EventDTO> = httpclient.get(apiUrl).body()

            if (events.isNotEmpty()) {
                eventCache.clear()
                eventCache.addAll(events)
                logger.info("Successfully loaded ${events.size} events into memory")
                return eventCache
            } else {
                logger.warn("API Returned an empty event list")
                return emptyList()
            }
        } catch (e: Exception) {
            logger.error("Failed to fetch events: ${e.message}")
            return emptyList()
        }
    }

    suspend fun getEventsByType(type: String? = null): List<EventDTO> = withContext(Dispatchers.Default) {
        if (eventCache.isEmpty()) logger.warn("No events loaded")

        if (type == null) {
            eventCache.toList()
        } else {
            eventCache.filter { it.eventType.equals(type, ignoreCase = true) }
        }
    }

    fun getAllEvents(): List<EventDTO> = eventCache.toList()
}