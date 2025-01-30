package np.com.parts

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.routing.*
import np.com.parts.plugins.*
import np.com.parts.system.Services.*
import np.com.parts.system.applicationRoutes

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    configureSerializationAndKoin()
    configureHTTP()
    configureDatabases()
    configureMonitoring()
    configureSecurity()
    configureRouting()


    val connection=connectToMongoDB(environment)
    val productsService = ProductService(connection)
    val userService = UserService(connection)
    val cartService = CartService(connection, productsService)

    val orderService = OrderService(connection, userService = userService, cartService = cartService)
    val pasteService = PasteService(connection)
    val paymentService = PaymentService(connection, orderService, userService)
    val categoryService = CategoryService(connection)
    val carrouselService = CarrouselService(connection)









    routing {
    applicationRoutes(
        productsService,
        orderService,
        userService,
        cartService,
        paymentService,
        pasteService,
        categoryService,
        carrouselService
    )

    }
}

