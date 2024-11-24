package np.com.parts.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import np.com.parts.API.Models.*
import np.com.parts.API.NetworkModule
import np.com.parts.API.Repository.OrderRepository

sealed class CheckoutState {
    object Loading : CheckoutState()
    data class Success(val order: OrderModel) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
    data class CartSummaryLoaded(
        val items: List<LineItem>,
        val summary: OrderSummary
    ) : CheckoutState()
}

class CheckoutViewModel(
) : ViewModel() {
    private val orderRepository by lazy {
        OrderRepository(NetworkModule.provideHttpClient())
    }


    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Loading)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState

    fun loadCartSummary() {
        viewModelScope.launch {
            try {
                _checkoutState.value = CheckoutState.Loading
                orderRepository.getCartSummary()
                    .onSuccess { cart ->
                        _checkoutState.value = CheckoutState.CartSummaryLoaded(
                            items = cart.items,
                            summary = cart.summary
                        )
                    }
                    .onFailure { exception ->
                        _checkoutState.value = CheckoutState.Error(
                            exception.message ?: "Failed to load cart summary"
                        )
                    }
            } catch (e: Exception) {
                _checkoutState.value = CheckoutState.Error(
                    e.message ?: "Failed to load cart summary"
                )
            }
        }
    }

    fun placeOrder(
        shippingDetails: ShippingDetails,
        paymentMethod: PaymentMethod,
        notes: String? = null
    ) {
        viewModelScope.launch {
            try {
                _checkoutState.value = CheckoutState.Loading
                
                val cartState = _checkoutState.value as? CheckoutState.CartSummaryLoaded
                    ?: throw IllegalStateException("Cart items not loaded")
                
                val request = CreateOrderRequest(
                    items = cartState.items,
                    paymentMethod = paymentMethod,
                    shippingDetails = shippingDetails,
                    notes = notes,
                    source = OrderSource.MOBILE_APP
                )

                orderRepository.createOrder(request)
                    .onSuccess { order ->
                        _checkoutState.value = CheckoutState.Success(order)
                    }
                    .onFailure { exception ->
                        _checkoutState.value = CheckoutState.Error(
                            exception.message ?: "Failed to place order"
                        )
                    }
            } catch (e: Exception) {
                _checkoutState.value = CheckoutState.Error(e.message ?: "Failed to place order")
            }
        }
    }
}