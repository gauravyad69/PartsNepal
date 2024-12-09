package np.com.parts.ViewModels

import android.annotation.SuppressLint
import android.app.Application
import android.net.http.HttpException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import np.com.parts.API.Models.*
import np.com.parts.API.NetworkModule
import np.com.parts.API.Repository.OrderRepository
import np.com.parts.App
import np.com.parts.database.AppDatabase
import np.com.parts.repository.CartRepository
import timber.log.Timber

sealed class CheckoutState {
    object Initial : CheckoutState()
    object Loading : CheckoutState()
    data class Success(val order: OrderModel) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {
    private val cartRepository: CartRepository = (application as App).cartRepository
    private val orderRepository: OrderRepository = OrderRepository(NetworkModule.provideHttpClient())

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Initial)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState

    private val _cartSummary = MutableStateFlow<OrderSummary?>(null)
    val cartSummary: StateFlow<OrderSummary?> = _cartSummary

    init {
        loadCartSummary()
    }

    private fun loadCartSummary() {
        viewModelScope.launch {
            try {
                cartRepository.getCartSummary()
                    .collect { summary ->
//                        _cartSummary.value = summary todo i dont know what to do with this
                        Timber.d("Cart summary loaded: $summary")
                    }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load cart summary")
                _checkoutState.value = CheckoutState.Error("Failed to load cart summary")
            }
        }
    }

    @SuppressLint("NewApi")
    fun placeOrder(
        shippingDetails: ShippingDetails,
        paymentMethod: PaymentMethod,
        notes: String? = null
    ) {
        // Use NonCancellable for critical operations
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            try {
                _checkoutState.value = CheckoutState.Loading
                Timber.i("Starting order placement process")

                // Create order request synchronously
                Timber.d("Creating order request with shipping details: $shippingDetails")
                val request = try {
                    Timber.d("Calling cartRepository.createOrder...")
                    withContext(NonCancellable) {
                        cartRepository.createOrder(shippingDetails, paymentMethod, notes)
                    }.also {
                        cartRepository.clearCart()
                        Timber.d("Order request created successfully: $it")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to create order request")
                    _checkoutState.value = CheckoutState.Error("Failed to create order request: ${e.message}")
                    return@launch
                }
                
                Timber.d("Sending order request to server...")
                
                // Place the order with timeout
                withTimeout(30000) { // 30 second timeout
                    Timber.d("Making API call to create order...")
                    val orderResult = withContext(NonCancellable) {
                        orderRepository.createOrder(request)
                    }
                    Timber.d("API call completed with result: $orderResult")
                    
                    when {
                        orderResult.isSuccess -> {
                            val order = orderResult.getOrNull()!!
                            Timber.d("Order created successfully, clearing cart...")
                            try {
                                withContext(NonCancellable) {
                                    cartRepository.clearCart()
                                }
                                _checkoutState.value = CheckoutState.Success(order)
                                Timber.i("Order placed successfully: ${order.orderNumber}")
                            } catch (e: Exception) {
                                Timber.e(e, "Failed to clear cart after successful order")
                                _checkoutState.value = CheckoutState.Success(order)
                            }
                        }
                        else -> {
                            val exception = orderResult.exceptionOrNull()!!
                            Timber.e(exception, "Server returned error during order placement")
                            _checkoutState.value = CheckoutState.Error(
                                when (exception) {
                                    is IOException -> "Network error: Please check your connection"
                                    is HttpException -> "Server error: ${exception.message}"
                                    else -> exception.message ?: "Failed to place order"
                                }
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during order placement")
                _checkoutState.value = CheckoutState.Error(
                    when (e) {
                        is TimeoutCancellationException -> "Request timed out. Please try again."
                        is IOException -> "Network error: Please check your connection"
                        is IllegalStateException -> "Invalid order state: ${e.message}"
                        else -> e.message ?: "Failed to place order"
                    }
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("CheckoutViewModel cleared")
    }
}