package np.com.parts.Domain.ViewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import np.com.parts.API.Models.OrderModel
import np.com.parts.API.Repository.OrderRepository
import javax.inject.Inject

sealed class OrderConfirmationState {
    object Loading : OrderConfirmationState()
    data class Success(val order: OrderModel) : OrderConfirmationState()
    data class Error(val message: String) : OrderConfirmationState()
}
@HiltViewModel
class OrderConfirmationViewModel @Inject constructor( private val orderRepository: OrderRepository
) : ViewModel() {


    private val _orderState = MutableStateFlow<OrderConfirmationState>(OrderConfirmationState.Loading)
    val orderState: StateFlow<OrderConfirmationState> = _orderState

    fun loadOrder(orderNumber: String) {
        viewModelScope.launch {
            _orderState.value = OrderConfirmationState.Loading
            orderRepository.getOrderDetails(orderNumber)
                .onSuccess { order ->
                    _orderState.value = OrderConfirmationState.Success(order)
                }
                .onFailure { exception ->
                    _orderState.value = OrderConfirmationState.Error(
                        exception.message ?: "Failed to load order"
                    )
                }
        }
    }
}