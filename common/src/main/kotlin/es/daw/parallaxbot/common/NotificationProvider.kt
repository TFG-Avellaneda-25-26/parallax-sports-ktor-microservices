package es.daw.parallaxbot.common

interface NotificationProvider {
    suspend fun notify(message: String, image: ByteArray)
}