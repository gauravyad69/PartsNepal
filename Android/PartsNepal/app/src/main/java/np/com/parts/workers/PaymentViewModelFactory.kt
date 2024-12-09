package np.com.parts.workers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import np.com.parts.API.Repository.KhaltiPaymentRepository
import np.com.parts.ViewModels.PaymentViewModel

class PaymentViewModelFactory(
    private val repository: KhaltiPaymentRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            return PaymentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
