import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import np.com.parts.API.Models.CreateOrderRequest
import np.com.parts.API.Models.OrderModel
import np.com.parts.API.NetworkModule
import np.com.parts.API.Repository.OrderRepository
import np.com.parts.API.Repository.UserRepository

class OrderViewModel : ViewModel() {
    private val orderRepository by lazy {
        OrderRepository(NetworkModule.provideHttpClient())
    }

    private val _orderState = MutableStateFlow<OrderState>(OrderState.Loading)
    val orderState: StateFlow<OrderState> = _orderState.asStateFlow()

    sealed class OrderState {
        object Loading : OrderState()
        data class Success(val orders: List<OrderModel>) : OrderState()
        data class Error(val message: String) : OrderState()
    }

    fun loadUserOrders() {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            orderRepository.getUserOrders()
                .onSuccess { orders ->
                    _orderState.value = OrderState.Success(orders)
                }
                .onFailure { error ->
                    _orderState.value = OrderState.Error(error.message ?: "Failed to load orders")
                }
        }
    }

    fun createOrder(orderRequest: CreateOrderRequest) {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            orderRepository.createOrder(orderRequest)
                .onSuccess { order ->
                    loadUserOrders()
                }
                .onFailure { error ->
                    _orderState.value = OrderState.Error(error.message ?: "Failed to create order")
                }
        }
    }

    fun getOrderDetails(orderId: String) {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            orderRepository.getOrderDetails(orderId)
                .onSuccess { order ->
                    _orderState.value = OrderState.Success(listOf(order))
                }
                .onFailure { error ->
                    _orderState.value = OrderState.Error(error.message ?: "Failed to load order details")
                }
        }
    }
} 