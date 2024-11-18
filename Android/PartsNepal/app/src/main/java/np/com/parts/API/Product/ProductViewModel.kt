package np.com.parts.API.Product

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import np.com.parts.system.models.BasicProductView
import np.com.parts.system.models.ProductModel
import np.com.parts.system.network.ProductApiClient

class ProductViewModel : ViewModel() {
    private val apiClient = ProductApiClient()
    private var recyclerViewState: Parcelable? = null
    private var currentPage = 1
    private var isLastPage = false
    private val pageSize = 10


    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore = _isLoadingMore.asStateFlow()

    private val _products = MutableStateFlow<List<ProductModel>>(emptyList())
    val products: StateFlow<List<ProductModel>> = _products.asStateFlow()

   private val _productById = MutableStateFlow<ProductModel?>(null)
    val productById: StateFlow<ProductModel?> = _productById.asStateFlow()

    private val _basicProducts = MutableStateFlow<List<BasicProductView>>(emptyList())
    val basicProducts = _basicProducts.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun saveScrollState(state: Parcelable) {
        recyclerViewState = state
    }

    fun getScrollState(): Parcelable? = recyclerViewState

    fun loadProducts(page: Int = 0) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            apiClient.getAllProducts(page = page)
                .onSuccess { response ->
                    _products.value = response.data
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }

            _loading.value = false
        }
    }

    fun loadProductsById(id: Int = 0) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            apiClient.getProductById(id)
                .onSuccess { response ->
                    _productById.value = response.data
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }

            _loading.value = false
        }
    }

    fun loadBasicProducts() {
        if (isLastPage || _isLoadingMore.value) return

        viewModelScope.launch {
            try {
                if (_basicProducts.value.isEmpty()) {
                    _loading.value = true  // Show shimmer for initial load
                } else {
                    _isLoadingMore.value = true  // Show loading indicator for pagination
                }


                _isLoadingMore.value = true
                val response = apiClient.getBasicProducts(currentPage, pageSize)
                // Assuming your API returns a list or some wrapper object
                // Adjust this according to your actual API response type
                   val newProducts = when {
                        // If API returns a list directly
                        response.isSuccess && response is List<*>  -> response as List<BasicProductView>

                        // If API returns a wrapper object with a data field
                        response.isSuccess && response != null   -> response as List<BasicProductView>

                        // If no data
                        else -> emptyList()
                }
                if (newProducts.isEmpty()) {
                    isLastPage = true
                } else {
                    val currentList = _basicProducts.value.toMutableList()
                    currentList.addAll(newProducts)
                    _basicProducts.value = currentList
                    currentPage++
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun resetPagination() {
        currentPage = 1
        isLastPage = false
        _basicProducts.value = emptyList()
    }

    // Similar functions for other API calls...
}