package np.com.parts

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import np.com.parts.plugins.*
import np.com.parts.system.Routes.Auth.configureAuthRoutes
import np.com.parts.system.Routes.Cart.cartRoutes
import np.com.parts.system.Services.*
import np.com.parts.system.applicationRoutes
import np.com.parts.system.Utils.TestDataSetup

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
    val userService = UserService(connection)
    val cartService = CartService(connection, productsService)

    configureAuthRoutes(userService = userService)
    val orderService = OrderService(connection, userService = userService, cartService = cartService)
    val pasteService = PasteService(connection)
    val paymentService = PaymentService(connection, orderService, userService)
    val categoryService = CategoryService(connection)

//    // Setup test data if in development environment
//    if (environment.developmentMode) {
//        val testDataSetup = TestDataSetup(
//            orderService = orderService,
//            productService = productsService,
//            userService = userService,
//            cartService = cartService
//        )
//
//        launch {
//            try {
//                testDataSetup.setupAll()
//                log.info("Test data setup completed successfully")
//            } catch (e: Exception) {
//                log.error("Failed to setup test data", e)
//            }
//        }
//    }









    routing {
    applicationRoutes(productsService, orderService, userService,  cartService, paymentService, pasteService, categoryService)

    }
}

