package np.com.parts.ViewModels

import CartEvent
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import np.com.parts.API.Models.*
import np.com.parts.repository.CartRepository
import np.com.parts.app_utils.SyncManager
import np.com.parts.app_utils.SyncStatus
import timber.log.Timber
import javax.inject.Inject

sealed class CartState {
    object Loading : CartState()
    data class Success(
        val items: List<LineItem>,
        val summary: OrderSummary,
        val syncStatus: SyncStatus = SyncStatus.IDLE
    ) : CartState()
    object Empty : CartState()
    data class Error(val message: String) : CartState()
}

sealed class CartAction {
    data class UpdateQuantity(val itemId: String, val quantity: Int) : CartAction()
    data class RemoveItem(val itemId: String) : CartAction()
    data class AddItem(val productId: Int, val quantity: Int, val product: ProductModel) : CartAction()
    object ClearCart : CartAction()
}

//sealed class CartEvent {
//    data class ShowMessage(val message: String) : CartEvent()
//    data class ItemAdded(val name: String) : CartEvent()
//    object NavigateToCart : CartEvent()
//}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val syncManager: SyncManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _cartState = MutableStateFlow<CartState>(CartState.Loading)
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    private val _cartEvents = MutableSharedFlow<CartEvent>()
    val cartEvents: SharedFlow<CartEvent> = _cartEvents.asSharedFlow()

    // Cart badge counter
    val cartItemCount: StateFlow<Int> = cartRepository.cartItemCount
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        loadCart()
        observeSyncStatus()
    }

    private fun loadCart() {
        viewModelScope.launch {
            combine(
                cartRepository.cartItems,
                cartRepository.getCartSummary()
            ) { items, summary ->
                if (items.isEmpty()) {
                    CartState.Empty
                } else {
                    CartState.Success(
                        items = items,
                        summary = OrderSummary(
                            subtotal = summary.subtotal,
                            shippingCost = summary.shippingCost,
                            total = summary.total
                        )
                    )
                }
            }.catch { e ->
                Timber.e(e, "Error loading cart")
                emit(CartState.Error(e.message ?: "Failed to load cart"))
            }.collect { state ->
                _cartState.value = state
            }
        }
    }

    private fun observeSyncStatus() {
        viewModelScope.launch {
            syncManager.getSyncStatus(context)
                .collect { syncStatus ->
                    val currentState = _cartState.value
                    if (currentState is CartState.Success) {
                        _cartState.value = currentState.copy(syncStatus = syncStatus)
                    }
                }
        }
    }

    fun dispatch(action: CartAction) {
        viewModelScope.launch {
            try {
                when (action) {
                    is CartAction.AddItem -> {
                        cartRepository.addToCart(
                            action.productId,
                            action.quantity,
                            action.product
                        ).onSuccess {
                            _cartEvents.emit(CartEvent.ItemAdded(action.product.basic.productName))
                        }.onFailure { e ->
                            _cartEvents.emit(CartEvent.ShowMessage(e.message ?: "Failed to add item"))
                        }
                    }

                    is CartAction.UpdateQuantity -> {
                        cartRepository.updateQuantity(action.itemId, action.quantity)
                            .onFailure { e ->
                                _cartEvents.emit(CartEvent.ShowMessage(e.message ?: "Failed to update quantity"))
                            }
                    }

                    is CartAction.RemoveItem -> {
                        cartRepository.removeFromCart(action.itemId)
                            .onFailure { e ->
                                _cartEvents.emit(CartEvent.ShowMessage(e.message ?: "Failed to remove item"))
                            }
                    }

                    is CartAction.ClearCart -> {
                        cartRepository.clearCart()
                            .onFailure { e ->
                                _cartEvents.emit(CartEvent.ShowMessage(e.message ?: "Failed to clear cart"))
                            }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error dispatching cart action")
                _cartEvents.emit(CartEvent.ShowMessage(e.message ?: "An error occurred"))
            }
        }
    }

    fun syncCart(): Flow<Result<Unit>> = flow {
        try {
            _cartEvents.emit(CartEvent.SyncEvent.Started)
            val result = cartRepository.syncCart()
            if (result.isSuccess) {
                _cartEvents.emit(CartEvent.SyncEvent.Completed)
            } else {
                val error = result.exceptionOrNull()
                _cartEvents.emit(CartEvent.SyncEvent.Failed(error?.message ?: "Unknown error"))
            }
            emit(result)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing cart")
            _cartEvents.emit(CartEvent.SyncEvent.Failed(e.message ?: "Failed to sync cart"))
            emit(Result.failure(e))
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Trigger final sync when ViewModel is cleared
        viewModelScope.launch {
            cartRepository.syncCart()
        }
    }

    private suspend fun validateCart(): Boolean {
        val currentState = _cartState.value
        if (currentState !is CartState.Success || currentState.items.isEmpty()) {
            _cartEvents.emit(CartEvent.ShowMessage("Cart is empty"))
            return false
        }
        return true
    }

    // Add this function to check cart status
    suspend fun validateCartForCheckout(): Boolean {
        return validateCart()
    }
}