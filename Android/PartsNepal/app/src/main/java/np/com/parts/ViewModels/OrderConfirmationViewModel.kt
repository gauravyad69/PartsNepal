package np.com.parts.ViewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import np.com.parts.API.Models.OrderModel
import np.com.parts.API.NetworkModule
import np.com.parts.API.Repository.OrderRepository

sealed class OrderConfirmationState {
    object Loading : OrderConfirmationState()
    data class Success(val order: OrderModel) : OrderConfirmationState()
    data class Error(val message: String) : OrderConfirmationState()
}

class OrderConfirmationViewModel(
) : ViewModel() {
    private val orderRepository by lazy {
        OrderRepository(NetworkModule.provideHttpClient())
    }

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