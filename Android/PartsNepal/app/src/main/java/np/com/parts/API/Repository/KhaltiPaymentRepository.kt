package np.com.parts.API.Repository

import android.content.Context
import android.widget.Toast
import com.khalti.checkout.Khalti
import com.khalti.checkout.data.Environment
import com.khalti.checkout.data.KhaltiPayConfig
import com.khalti.checkout.data.PaymentResult
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import np.com.parts.API.Models.KhaltiPaymentResponse
import np.com.parts.API.Models.PaymentRequestModel
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class KhaltiPaymentRepository @Inject constructor(
    private val client: HttpClient
) {


    suspend fun startKhaltiPayment(
        context: Context,
        purchaseOrderName: String,
        onResult: (PaymentResult) -> Unit
    ) {
        Timber.i("Starting Khalti payment for order: $purchaseOrderName")
        
        try {
            val request = PaymentRequestModel(
                purchase_order_name = purchaseOrderName
            )

            Timber.d("Making POST request to /khalti/start")
            val response = client.post("/khalti/start") {
                setBody(request)
            }
            Timber.d("Response received: ${response.status}")

            if (response.status.value == 200) {
                val body = response.body<KhaltiPaymentResponse>()
                Timber.d("Successfully received PIDX: ${body.pidx}")

                val config = KhaltiPayConfig(
                    publicKey = "844fa3176bb04faba9b9791d8e0b6c13",
                    pidx = body.pidx,
                    environment = Environment.TEST
                )

                val khalti = Khalti.init(
                    context = context,
                    config = config,
                    onPaymentResult = { paymentResult, khalti ->
                        Timber.d("Payment Result received: ${paymentResult.status}")
                        onResult(paymentResult)
                        if (paymentResult.payload != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val ver = verification(
                                        paymentResult.payload!!.pidx.toString(),
                                        paymentResult.payload!!.purchaseOrderName.toString()
                                    )
                                    if (ver) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Payment Verified, Updating Status", Toast.LENGTH_SHORT).show()
                                            khalti.close()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Error during payment verification")
                                }
                            }
                        }
                    },
                    onMessage = { payload, _ ->
                        Timber.d("Khalti message: ${payload.message}")
                    },
                    onReturn = { _ ->
                        Timber.d("Payment canceled or dialog dismissed")
                        onResult(PaymentResult("Canceled", null))
                    }
                )
                khalti.open()
            } else {
                Timber.e("Failed to initiate Khalti payment, status: ${response.status}")
                onResult(PaymentResult("Failed", null))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during Khalti payment process")
            onResult(PaymentResult("Failed: ${e.message}", null))
        }
    }

    suspend fun verification(pidx: String, orderNumber: String): Boolean{
        val response = client.get("/khalti/verify") {
            parameter("pidx", pidx)
            parameter("orderNumber", orderNumber)
        }

        return response.status.value == 200
    }


}

