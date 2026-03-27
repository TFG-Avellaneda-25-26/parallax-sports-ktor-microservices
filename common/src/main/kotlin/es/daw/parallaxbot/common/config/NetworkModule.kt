package es.daw.parallaxbot.common.config

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.server.config.ApplicationConfig

/**
 * Shared outbound HTTP client configuration for internal service-to-service calls.
 */
val networkModule = module {
    single {

        val config = get<ApplicationConfig>()
        val apiKey = config.property("parallaxbot.api.key").getString()

        // -> Source: Service Startup || Action: Build reusable CIO HttpClient singleton || Strategy: pooled connections with content negotiation and bounded endpoint limits
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }

//            defaultRequest {
//                header("X-Api-Key", apiKey)
//            }

            engine {
                maxConnectionsCount = 1000

                endpoint {
                    maxConnectionsPerRoute = 100
                    pipelineMaxSize = 20
                    keepAliveTime = 5000
                    connectTimeout = 5000
                }
            }
        }
    }
}