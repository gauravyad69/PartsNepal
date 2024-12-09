package np.com.parts.ViewModels
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalti.checkout.data.PaymentPayload
import com.khalti.checkout.data.PaymentResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import np.com.parts.API.Repository.KhaltiPaymentRepository

class PaymentViewModel(private val paymentRepository: KhaltiPaymentRepository) : ViewModel() {

    // StateFlow to hold payment status
    private val _paymentState = MutableStateFlow<PaymentResult?>(null)
    val paymentState: StateFlow<PaymentResult?> get() = _paymentState

    // Process payment with error handling and result propagation
    fun processPayment(purchaseOrderName: String, context: Context) {
        viewModelScope.launch {
            try {
                paymentRepository.startKhaltiPayment(purchaseOrderName, context = context) { result ->
                    _paymentState.value = result
                }
            } catch (e: Exception) {
                _paymentState.value = PaymentResult("Failed", e.message as PaymentPayload?)
            }
        }
    }
}