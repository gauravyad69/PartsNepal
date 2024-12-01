package np.com.parts.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import np.com.parts.API.BASE_URL
import np.com.parts.API.NetworkModule
import np.com.parts.API.TokenManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
   @Singleton
   fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
       return TokenManager(context)
   }
    @Provides
   @Singleton
   fun provideHttpClient(tokenManager: TokenManager): HttpClient {
       return HttpClient(CIO) {
           install(ContentNegotiation) {
               json(Json {
                   ignoreUnknownKeys = true
                   isLenient = true
                   prettyPrint = true
                   encodeDefaults = true
                   coerceInputValues = true
               })
           }
            install(DefaultRequest) {
               url(BASE_URL)
               contentType(ContentType.Application.Json)
               
               // Add JWT token to all requests if available
               tokenManager.getToken()?.let { token ->
                   header("Authorization", "Bearer $token")
               }
           }
            // ... rest of your client configuration
       }
   }
}
