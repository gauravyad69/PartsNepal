import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import np.com.parts.API.Models.CreateOrderRequest
import np.com.parts.API.Models.Order
import np.com.parts.API.Repository.OrderRepository

class OrderViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _selectedOrder = MutableStateFlow<Order?>(null)
    val selectedOrder: StateFlow<Order?> = _selectedOrder.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

   fun loadUserOrders() {
        viewModelScope.launch {
            _loading.value = true
            orderRepository.getUserOrders()
                .onSuccess { orderList ->
                    _orders.value = orderList
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            _loading.value = false
        }
    }

    fun createOrder(orderRequest: CreateOrderRequest) {
        viewModelScope.launch {
            _loading.value = true
            orderRepository.createOrder(orderRequest)
                .onSuccess { success ->
                    if (success) {
                        loadUserOrders() // Refresh orders list
                    } else {
                        _error.value = "Failed to create order"
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            _loading.value = false
        }
    }

    fun loadOrderDetails(orderNumber: String) {
        viewModelScope.launch {
            _loading.value = true
            orderRepository.getOrderDetails(orderNumber)
                .onSuccess { order ->
                    _selectedOrder.value = order
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            _loading.value = false
        }
    }
} 