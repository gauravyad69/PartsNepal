package np.com.parts.plugins

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureHTTP() {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }
    install(CORS) {

        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.AccessControlAllowHeaders)
        allowHeader(HttpHeaders.AccessControlAllowMethods)
        allowHeader(HttpHeaders.AccessControlAllowCredentials)
        allowHeader(HttpHeaders.ContentType)

        allowHeader("X-Requested-With")
        allowHost("super-duper-space-umbrella-5xjjpg66g44hwg-8080.app.github.dev", schemes = listOf("https"))


        allowHost("tap-swap-three.vercel.app", schemes = listOf("https"))


        // Allow cookies and credentials
        allowCredentials = true
        allowNonSimpleContentTypes = true
        allowSameOrigin = true
       allowHost("*.vercel.app", schemes = listOf("https"))
        // In production, replace this with your specific allowed origins
//        anyHost()//todo fix this fucking thing, very important for security
        allowHost("192.168.0.7")
        anyHost()
        print("installed cors successfully")

        ////



    }
//    install(ConditionalHeaders)
//    install(Compression)//todo enable these for production
//    install(CachingHeaders) {
//        options { call, outgoingContent ->
//            when (outgoingContent.contentType?.withoutParameters()) {
//                ContentType.Text.CSS -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
//                else -> null
//            }
//        }
//    }
}
