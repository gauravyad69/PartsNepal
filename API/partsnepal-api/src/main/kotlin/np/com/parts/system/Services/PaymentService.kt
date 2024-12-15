package np.com.parts.system.Services

import  com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import np.com.parts.NetworkModule
import np.com.parts.system.Models.*
import org.litote.kmongo.getCollection
import org.litote.kmongo.updateOne

class PaymentService(
    private val database: MongoDatabase,
    private val orderService: OrderService,
    private val userService: UserService,
) {
    private val collection: MongoCollection<Transactions>

    init {
        try {
            database.createCollection("transactions")
        } catch (e: Exception) {
            // Collection already exists
        }
        collection = database.getCollection<Transactions>("transactions")
        
        // Create indexes
        collection.createIndex(Indexes.ascending("userId"))
        collection.createIndex(
            Indexes.compoundIndex(
                Indexes.ascending("userId"),
                Indexes.ascending("phoneNumber")
            )
        )
    }






    val client =NetworkModule.provideHttpClientForKhalti()


// Add these functions to your existing CartService class

    suspend fun startKhalti(userId: Int, paymentRequestModel: PaymentRequestModel): KhaltiPaymentResponse = withContext(Dispatchers.IO) {
        try {
            println("Starting Khalti For $userId with ${paymentRequestModel}")

            val customerInfo = userService.getUserById(UserId(userId))!!
            val orderInfo= orderService.getOrderByNumber(paymentRequestModel.purchase_order_name)

            val requestBody=KhaltiPaymentRequestAsClient(
                    amount = orderInfo!!.summary.total.amount.toInt(),
                    purchase_order_id = orderInfo.orderNumber,
                    purchase_order_name = paymentRequestModel.purchase_order_name,
//                    amount_breakdown = orderInfo.items.map { items ->
//                        KhaltiAmountBreakdownAsClient(
//                            label = items.name,
//                            amount = (items.unitPrice.amount*items.quantity ).toKhaltiAmount()
//                        )
//                    },
                    customer_info = KhaltiCustomerInfoAsClient(
                        customerInfo.fullName,
                        customerInfo.email.value ?: "error@gmail.com",
                        customerInfo.phoneNumber.value
                    ),
                    product_details = orderInfo.items.map { item ->
                    KhaltiProductDetailAsClient(
                        identity = item.id,
                        name = item.name,
                        total_price = item.totalPrice.amount.toKhaltiAmount(), // Assuming each item has price and quantity
                        quantity = item.quantity,
                        unit_price = item.unitPrice.amount.toKhaltiAmount()
                    )
                }


                )

            val response = client.post("https://a.khalti.com/api/v2/epayment/initiate/"){
//                header("Authorization", "key 0d189d52c15041d781b0907abf346724")
                setBody(requestBody)
            }
//            val responseText = response.bodyAsText()
//            println("Raw Response: $responseText")

//            println("Response: $response")

                val body = response.body<KhaltiPaymentResponse>()
            println("body: $body")
            if (response.status== HttpStatusCode.OK){
                val pidxCreated = PidxCreated(
                    pidx = body.pidx,
                    orderName = paymentRequestModel.purchase_order_name,
                    description = orderInfo.customer.name
                    )
                addPidx(UserId(userId), pidx=pidxCreated)
            }

           body
        } catch (e: Exception) {
            println("Error in khalti start: ${e.message}")
            println("Error: ${e.localizedMessage}")
            e.printStackTrace()
            KhaltiPaymentResponse(
                pidx="error at server",
                payment_url = "error at server",
                expires_in = 0,
                expires_at = ""
            )
        }
    }



    suspend fun verificationOfPayment(userId: Int, pidx: String, orderNumber: String): Boolean = withContext(Dispatchers.IO) {
        try {
            println("paymentService.verificationOfPayment - Starting Request userId: $userId")

            val customerInfo = userService.getUserById(UserId(userId))!!
            val orderInfo= orderService.getOrderByNumber(orderNumber)!!


            val requestBody= VerificationRequest(
                pidx = pidx
            )

            val response = client.post("https://a.khalti.com/api/v2/epayment/lookup/"){
//                header("Authorization", "key 0d189d52c15041d781b0907abf346724")
                setBody(requestBody)
            }
//            val responseText = response.bodyAsText()
//            println("Raw Response: $responseText")

            val temu = response.body<VerificationResponse>()



            when(temu.status){
                "Initiated"->orderService.updatePaymentStatus(orderNumber, PaymentStatus.INITIATED, pidx)
                "Completed"->orderService.updatePaymentStatus(orderNumber, PaymentStatus.COMPLETED, pidx)
                "Pending"->orderService.updatePaymentStatus(orderNumber, PaymentStatus.ON_HOLD, pidx)
                "Refunded"->orderService.updatePaymentStatus(orderNumber, PaymentStatus.REFUNDED, pidx)
                "Partially Refunded"->orderService.updatePaymentStatus(orderNumber, PaymentStatus.REFUNDED, pidx)
                "Expired"->orderService.updatePaymentStatus(orderNumber, PaymentStatus.FAILED, pidx)
                "User canceled"->orderService.updatePaymentStatus(orderNumber, PaymentStatus.FAILED, pidx)
            }

            if (temu.status.equals("Completed")) {
                val updateOrderStatus=orderService.updateOrderStatus(orderNumber, OrderStatus.PAYMENT_CONFIRMED, "SYSTEM")
                val updateTrackingStatus=orderService.updateOrderTrackingStatus(orderNumber, OrderStatus.PAYMENT_CONFIRMED, "SYSTEM")

                println("UPDATED ORDER STATUS AND TRACKING STATUS $updateTrackingStatus, $updateOrderStatus")

                // Find existing transaction document for user or create new one
                val filter = Filters.eq("userId", UserId(userId))
                
                val newTransaction = PaidTransactions(
                    pidx = pidx,
                    items = orderInfo.items,
                    amount = orderInfo.summary.total.amount.toKhaltiAmount(),
                    amount_breakdown = orderInfo.items.map { items ->
                        KhaltiAmountBreakdownAsClient(
                            label = items.name,
                            amount = (items.unitPrice.amount * items.quantity).toKhaltiAmount()
                        )
                    }
                )

                val newPidx = PidxCreated(
                    pidx = pidx,
                    orderName = orderNumber
                )

                // Update using $push to add to arrays
                val update = Updates.combine(
                    Updates.setOnInsert("userId", UserId(userId)),
                    Updates.setOnInsert("phoneNumber", userService.getUserPhoneNumberById(UserId(userId))!!),
                    Updates.push("paidTransactions", newTransaction),
                    Updates.push("pidxCreated", newPidx)
                )

                // Use upsert to create document if it doesn't exist
                val options = UpdateOptions().upsert(true)
                collection.updateOne(filter, update, options)
            }

            return@withContext true
        } catch (e: Exception) {
            println("Error in verificationOfPayment: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    suspend fun verificationOfPaymentFromKhalti(
        pidx: String,
        orderNumber: String,
        amount: Int,
        total_amount: Int,
        tidx: String,
        transaction_id: String,
        mobile: String,
        status: String,
        purchase_order_id: String,
        purchase_order_name: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            println("paymentService.verificationOfPayment - Fetching as it was called")
            val userInfo= userService.getUserByPhone(PhoneNumber(mobile))!!
            val orderInfo = orderService.getOrderByNumber(orderNumber)!!

            val requestBody = VerificationRequest(pidx = pidx)

            val response = client.post("https://a.khalti.com/api/v2/epayment/lookup/") {
                setBody(requestBody)
            }
            val responseText = response.bodyAsText()
            println("Raw Response: $responseText")

            val temu = response.body<VerificationResponse>()

            // Update order status
            when(temu.status) {
                "Initiated" -> orderService.updatePaymentStatus(orderNumber, PaymentStatus.INITIATED, pidx)
                "Completed" -> orderService.updatePaymentStatus(orderNumber, PaymentStatus.COMPLETED, pidx)
                "Pending" -> orderService.updatePaymentStatus(orderNumber, PaymentStatus.ON_HOLD, pidx)
                "Refunded" -> orderService.updatePaymentStatus(orderNumber, PaymentStatus.REFUNDED, pidx)
                "Partially Refunded" -> orderService.updatePaymentStatus(orderNumber, PaymentStatus.REFUNDED, pidx)
                "Expired" -> orderService.updatePaymentStatus(orderNumber, PaymentStatus.FAILED, pidx)
                "User canceled" -> orderService.updatePaymentStatus(orderNumber, PaymentStatus.FAILED, pidx)
            }

            // Check for payment tampering
            if (!temu.status.equals(status) || !temu.transaction_id.equals(transaction_id)) {
                println("malicious user detected, payment has been tampered, account suspended")
                userService.updateAccountStatus(userInfo.user.userId, AccountStatus.SUSPENDED)
                return@withContext false
            }

            if (temu.status.equals("Completed")) {
                val filter = Filters.eq("userId", userInfo.user.userId)

                val newTransaction = PaidTransactions(
                    pidx = pidx,
                    items = orderInfo.items,
                    amount = orderInfo.summary.total.amount.toInt(),
                    amount_breakdown = orderInfo.items.map { items ->
                        KhaltiAmountBreakdownAsClient(
                            label = items.name,
                            amount = (items.unitPrice.amount.toInt() * items.quantity)
                        )
                    }
                )

                val newPidx = PidxCreated(
                    pidx = pidx,
                    orderName = orderNumber
                )

                // Update using $push to add to arrays
                val update = Updates.combine(
                    Updates.setOnInsert("userId", userInfo.user.userId),
                    Updates.setOnInsert("phoneNumber", userInfo.user.phoneNumber),
                    Updates.push("paidTransactions", newTransaction),
                    Updates.push("pidxCreated", newPidx)
                )

                // Use upsert to create document if it doesn't exist
                val options = UpdateOptions().upsert(true)
                collection.updateOne(filter, update, options)
                println("Updated transaction document for user: ${userInfo.user.userId}")
            }

            return@withContext true
        } catch (e: Exception) {
            println("Error in verificationOfPaymentFromKhalti: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }



    suspend fun addPidx(userId: UserId, pidx: PidxCreated): Boolean = withContext(Dispatchers.IO) {
        val filter = Filters.eq("userId", userId)
        val update = Updates.combine(
            Updates.setOnInsert("userId", userId),
            Updates.setOnInsert("phoneNumber", userService.getUserPhoneNumberById(userId)!!),
            Updates.push("pidxCreated", pidx)
        )
        
        val options = UpdateOptions().upsert(true)
        val result = collection.updateOne(filter, update, options)
        result.modifiedCount > 0 || result.upsertedId != null
    }
}


@Serializable
data class VerificationRequest(
    val pidx: String
)

@Serializable
data class VerificationResponse(
    val pidx: String,
    val total_amount: Int,
    val status: String,
    val transaction_id: String,
    val fee: Int,
    val refunded: Boolean
)