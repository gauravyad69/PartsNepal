package np.com.parts

import io.ktor.server.application.*
import io.ktor.server.routing.*
import np.com.parts.plugins.*
import np.com.parts.system.Services.OrderService
import np.com.parts.system.Services.ProductService
import np.com.parts.system.Services.UserService
import np.com.parts.system.applicationRoutes

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureHTTP()
    configureDatabases()
    configureMonitoring()
    configureSecurity()
    configureRouting()
    val connection=connectToMongoDB()
    val productsService = ProductService(connection)
    val orderService = OrderService(connection)
    val userService = UserService(connection)

    routing {
    applicationRoutes(productsService, orderService, userService)
    }
}

