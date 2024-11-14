package np.com.parts.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.event.*

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        format { call ->
            val httpMethod = call.request.httpMethod.value
            val path = call.request.path()
            val status = call.response.status()?.value ?: "unknown"

            // Add color for different HTTP methods (ANSI color codes)
            val methodColor = when (httpMethod) {
                "GET" -> "\u001B[32m"   // Green for GET
                "POST" -> "\u001B[34m"  // Blue for POST
                "PUT" -> "\u001B[33m"   // Yellow for PUT
                "DELETE" -> "\u001B[31m" // Red for DELETE
                else -> "\u001B[0m"     // Reset color
            }

            // Reset color after the method
            val reset = "\u001B[0m"

            // Log format: HTTP Method (with color), Path, and Response Status
            "$methodColor$httpMethod$reset $path - Status: $status"
        }
        filter { call -> call.request.path().startsWith("/") }
    }
    install(CallId) {
        header(HttpHeaders.XRequestId)
        verify { callId: String ->
            callId.isNotEmpty()
        }
    }
}
