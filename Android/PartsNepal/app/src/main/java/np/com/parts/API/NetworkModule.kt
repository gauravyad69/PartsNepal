package np.com.parts.API

import android.content.Context
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.observer.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import np.com.parts.API.Models.configureSslValidation
import timber.log.Timber
import java.security.SecureRandom
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext

const val BASE_URL = "http://192.168.0.8:9090"
const val PRODUCTS_PATH = "$BASE_URL/products"

object NetworkModule {
    private var tokenManager: TokenManager? = null

    fun initialize(context: Context) {
        tokenManager = TokenManager.getInstance(context)
    }

    fun provideHttpClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
                encodeDefaults = true
                coerceInputValues = true  // Add this
            })
        }



            install(DefaultRequest) {
            url(BASE_URL)
            contentType(ContentType.Application.Json)
            
            // Add JWT token to all requests if available
            tokenManager?.getToken()?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Timber.tag("Ktor").d(message)
                }
            }
            level = LogLevel.INFO
        }

        install(ResponseObserver) {
            onResponse { response ->
                // Handle 401 Unauthorized responses
                if (response.status == HttpStatusCode.Unauthorized) {
                    tokenManager?.clearToken()
                    // You might want to emit an event to navigate to login screen
                }
            }
        }

        engine {
            requestTimeout = 15_000
        }
    }
}