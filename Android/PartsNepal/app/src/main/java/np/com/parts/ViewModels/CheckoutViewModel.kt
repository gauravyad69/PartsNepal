package np.com.parts.ViewModels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.http.HttpException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import np.com.parts.API.Models.*
import np.com.parts.API.NetworkModule
import np.com.parts.API.Repository.OrderRepository
import np.com.parts.App
import np.com.parts.database.AppDatabase
import np.com.parts.repository.CartRepository
import timber.log.Timber
import javax.inject.Inject

sealed class CheckoutState {
    object Initial : CheckoutState()
    object Loading : CheckoutState()
    data class Success(val order: OrderModel) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}

@Serializable
data class LocationData(
    val provinceList: List<Province>
)

@Serializable
data class Province(
    val id: Int,
    val name: String,
    val districtList: List<District>
)

@Serializable
data class District(
    val id: Int,
    val name: String,
    val municipalityList: List<Municipality>
)

@Serializable
data class Municipality(
    val id: Int,
    val name: String
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(private val cartRepository: CartRepository, private val orderRepository: OrderRepository,
                                            @ApplicationContext context: Context) : ViewModel() {

                                                private var context = context
    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Initial)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState

    private val _cartSummary = MutableStateFlow<OrderSummary?>(null)
    val cartSummary: StateFlow<OrderSummary?> = _cartSummary

    private val _provinces = MutableStateFlow<List<Province>>(emptyList())
    val provinces: StateFlow<List<Province>> = _provinces

    private val _selectedProvince = MutableStateFlow<Province?>(null)
    val selectedProvince: StateFlow<Province?> = _selectedProvince

    private val _selectedDistrict = MutableStateFlow<District?>(null)
    val selectedDistrict: StateFlow<District?> = _selectedDistrict

    init {
        loadCartSummary()
        loadLocationData()
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

    private fun loadLocationData() {
        viewModelScope.launch {
            try {
                val jsonString = context.assets.open("data.json").bufferedReader().use { it.readText() }
                val data = Json.decodeFromString<LocationData>(jsonString)
                _provinces.value = data.provinceList
            } catch (e: Exception) {
                Timber.e(e, "Error loading location data")
            }
        }
    }

    fun setSelectedProvince(province: Province) {
        _selectedProvince.value = province
        _selectedDistrict.value = null // Reset district when province changes
    }

    fun setSelectedDistrict(district: District) {
        _selectedDistrict.value = district
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