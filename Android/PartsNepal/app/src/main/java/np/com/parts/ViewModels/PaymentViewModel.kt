package np.com.parts.ViewModels
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalti.checkout.data.PaymentResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import np.com.parts.API.Repository.KhaltiPaymentRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val repository: KhaltiPaymentRepository
) : ViewModel() {

    // StateFlow to hold payment status
    private val _paymentState = MutableStateFlow<PaymentResult?>(null)
    val paymentState: StateFlow<PaymentResult?> get() = _paymentState

    // Process payment with error handling and result propagation
    fun processPayment(purchaseOrderName: String, context:Context) {
        Timber.i("Processing payment for order: $purchaseOrderName")
        viewModelScope.launch {
            try {
                Timber.d("Calling repository.startKhaltiPayment")
                repository.startKhaltiPayment(purchaseOrderName = purchaseOrderName, context=context) { result ->
                    Timber.d("Payment result received: ${result.status}")
                    _paymentState.value = result
                }
            } catch (e: Exception) {
                Timber.e(e, "Error processing payment")
                _paymentState.value = PaymentResult("Failed: ${e.message}", null)
            }
        }
    }

}