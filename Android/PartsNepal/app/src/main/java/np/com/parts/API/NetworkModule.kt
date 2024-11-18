package np.com.parts.API

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import timber.log.Timber

object NetworkModule {

    fun provideHttpClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(WebSockets)

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Timber.tag("Ktor").d(message)
                }
            }
            level=LogLevel.ALL
        }
//
//        install(io.ktor.client.plugins.defaultRequest.DefaultRequest) {
//            url(BASE_URL)
//            headers {
//                append("Accept", "application/json")
//            }
//        }

        engine {
            requestTimeout = 15_000
        }
    }
}