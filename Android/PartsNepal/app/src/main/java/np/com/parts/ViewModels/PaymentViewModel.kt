package np.com.parts.ViewModels
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khalti.checkout.data.PaymentPayload
import com.khalti.checkout.data.PaymentResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import np.com.parts.API.Repository.KhaltiPaymentRepository
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val repository: KhaltiPaymentRepository
) : ViewModel() {

    // StateFlow to hold payment status
    private val _paymentState = MutableStateFlow<PaymentResult?>(null)
    val paymentState: StateFlow<PaymentResult?> get() = _paymentState

    // Process payment with error handling and result propagation
    fun processPayment(purchaseOrderName: String) {
        viewModelScope.launch {
            try {
                repository.startKhaltiPayment(purchaseOrderName) { result ->
                    _paymentState.value = result
                }
            } catch (e: Exception) {
                _paymentState.value = PaymentResult("Failed file: paymentviewmodel line 31")
            }
        }
    }
}