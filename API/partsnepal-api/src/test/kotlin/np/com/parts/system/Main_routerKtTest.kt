package np.com.parts.system

import io.ktor.client.request.*
import io.ktor.server.testing.*
import kotlin.test.Test

class Main_routerKtTest {

@Test
fun testGetPaymentsuccess() = testApplication {
    application {
        // Assuming you have a function to configure your Ktor application
    }
    client.get("/payment-success").apply {

    }
}
}