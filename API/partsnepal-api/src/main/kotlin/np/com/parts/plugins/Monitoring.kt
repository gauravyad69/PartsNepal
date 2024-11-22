package np.com.parts.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.event.*
import ch.qos.logback.classic.LoggerContext
import org.slf4j.LoggerFactory
import kotlinx.coroutines.runBlocking

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val path = call.request.path()
            
            // Get request body
            val requestBody = runBlocking {
                try {
                    if (call.request.contentType().match(ContentType.Application.Json)) {
                        call.receiveText()
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

    // Configure MongoDB logging
    (LoggerFactory.getILoggerFactory() as LoggerContext).apply {
        getLogger("org.mongodb.driver").level = ch.qos.logback.classic.Level.INFO
        getLogger("org.mongodb.driver.connection").level = ch.qos.logback.classic.Level.WARN
        getLogger("org.mongodb.driver.management").level = ch.qos.logback.classic.Level.WARN
        getLogger("org.mongodb.driver.cluster").level = ch.qos.logback.classic.Level.WARN
        getLogger("org.mongodb.driver.protocol.command").level = ch.qos.logback.classic.Level.INFO
    }
}
