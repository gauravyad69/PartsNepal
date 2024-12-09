package np.com.parts


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

//const val BASE_URL_KHALTI = "https://a.khalti.com/api/v2"

object NetworkModule {

    fun provideHttpClientForKhalti(): HttpClient = HttpClient(CIO) {
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
//            url(BASE_URL_KHALTI)
            contentType(ContentType.Application.Json)
            headers{
                append("Authorization", "key 0d189d52c15041d781b0907abf346724")
            }

        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println(message)
                }
            }
            level = LogLevel.INFO
        }

        install(ResponseObserver) {
            onResponse { response ->
                // Handle 401 Unauthorized responses
                if (response.status == HttpStatusCode.Unauthorized) {
                    println("got 401 Unauthorized")
                }
            }
        }

        engine {
            requestTimeout = 15_000
        }
    }
}