package np.com.parts.API.Repository

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.lifecycle.Lifecycle
import com.google.android.material.snackbar.Snackbar
import com.khalti.checkout.Khalti
import com.khalti.checkout.data.Environment
import com.khalti.checkout.data.KhaltiPayConfig
import com.khalti.checkout.data.PaymentResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import np.com.parts.API.Models.KhaltiPaymentResponse
import np.com.parts.API.Models.PaymentRequestModel
import np.com.parts.API.NetworkModule
import timber.log.Timber


class KhaltiPaymentRepository(private val contextMain: Context) {

    val client: HttpClient= NetworkModule.provideHttpClient()

    suspend fun startKhaltiPayment(
        purchaseOrderName: String,
        context: Context,
        onResult: (PaymentResult) -> Unit
    ) {
        val request = PaymentRequestModel(
            purchase_order_name = purchaseOrderName
        )

        val response = client.post("/khalti/start") {
            setBody(request)
        }

        if (response.status.value == 200) {
            val body = response.body<KhaltiPaymentResponse>()

            // Create Khalti Config
            val config = KhaltiPayConfig(
                publicKey = "844fa3176bb04faba9b9791d8e0b6c13",
                pidx = body.pidx,
                environment = Environment.TEST
            )

            // Show Khalti Checkout Dialog
            val khalti = Khalti.init(
                context = contextMain,
                config = config,
                onPaymentResult = { paymentResult, khalti ->
                    Timber.i("Payment Result: ${paymentResult.message}, code: ${paymentResult.status}, totalAmount: ${paymentResult.payload?.totalAmount}")
                    onResult(paymentResult)
                    CoroutineScope(Dispatchers.IO).launch {
                        val ver =verification(paymentResult.payload!!.pidx.toString(),paymentResult.payload!!.purchaseOrderName.toString() )
                        if (ver) Toast.makeText(contextMain, "Payment Verified, Updating Status", Toast.LENGTH_SHORT).show()
                        if (ver) khalti.close()
                    }
                },
                onMessage = { payload, khalti ->
                    Timber.i("OnMessage: ${payload.message}, event: ${payload.event}, throwable: ${payload.throwable}")
                },
                onReturn = { khalti ->
                    Timber.i("Payment canceled or dialog dismissed")
                    onResult(PaymentResult("Canceled", null))
                }
            )
            khalti.open()
        } else {
            Timber.e("Failed to initiate Khalti payment, status: ${response.status}")
            onResult(PaymentResult("Failed", null))
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

