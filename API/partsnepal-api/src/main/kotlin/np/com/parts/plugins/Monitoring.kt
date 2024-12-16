package np.com.parts.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.event.*
import ch.qos.logback.classic.LoggerContext
import com.codahale.metrics.Slf4jReporter
import io.ktor.server.metrics.dropwizard.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import java.util.concurrent.TimeUnit

fun Application.configureMonitoring() {
    install(CallId) {
        header(HttpHeaders.XRequestId)
        verify { callId: String ->
            callId.isNotEmpty()
        }
    }

    install(CallLogging) {
        level = Level.INFO
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val path = call.request.path()

            // Get request body and handle serialization errors
            val requestBody = runBlocking {
                try {
                    if (call.request.contentType().match(ContentType.Application.Json)) {
                        // Deserialize and handle potential serialization errors
                        try {
                            val bodyText = call.receiveText()
                            // Try deserializing here if needed
                            bodyText
                        } catch (e: kotlinx.serialization.SerializationException) {
                            "Serialization error: ${e.message}"
                        }
                    } else {
                        "<binary-content>"
                    }
                } catch (e: Exception) {
                    "<no-body>"
                }
            }

            buildString {
                appendLine("$httpMethod $path")
                appendLine("Status: $status")
                appendLine("Request Body: $requestBody")
                appendLine("Response: ${status?.description}")
            }
        }

        // Skip health checks and static content
        filter { call ->
            !call.request.path().startsWith("/health") &&
                    !call.request.path().startsWith("/static")
        }
    }

    // Optional: Set up StatusPages to handle and log errors globally
    install(StatusPages) {
        exception<SerializationException> { call, cause ->
            call.respondText(status=HttpStatusCode.BadRequest, text="Serialization error: ${cause.message}")
        }
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
    // Configure MongoDB logging
    (LoggerFactory.getILoggerFactory() as LoggerContext).apply {
        getLogger("org.mongodb.driver").level = ch.qos.logback.classic.Level.INFO
        getLogger("org.mongodb.driver.connection").level = ch.qos.logback.classic.Level.WARN
        getLogger("org.mongodb.driver.management").level = ch.qos.logback.classic.Level.WARN
        getLogger("org.mongodb.driver.cluster").level = ch.qos.logback.classic.Level.WARN
        getLogger("org.mongodb.driver.protocol.command").level = ch.qos.logback.classic.Level.INFO
    }
//    install(DropwizardMetrics) {
//        Slf4jReporter.forRegistry(registry)
//            .outputTo(log)
//            .convertRatesTo(TimeUnit.SECONDS)
//            .convertDurationsTo(TimeUnit.MILLISECONDS)
//            .build()
//            .start(10, TimeUnit.SECONDS)
//    }

}
