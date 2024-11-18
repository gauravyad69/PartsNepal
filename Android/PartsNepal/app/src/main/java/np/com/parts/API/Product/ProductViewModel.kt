package np.com.parts.API.Product

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                withContext(Dispatchers.Main) {
                    if (_basicProducts.value.isEmpty()) {
                        _loading.value = true
                    } else {
                        _isLoadingMore.value = true
                    }
                }

                // Make API call on IO dispatcher
                withContext(Dispatchers.IO) {
                    apiClient.getBasicProducts(currentPage, pageSize)
                        .onSuccess { response ->
                            withContext(Dispatchers.Main) {
                                val newProducts = response.data
                                if (newProducts.isEmpty()) {
                                    isLastPage = true
                                } else {
                                    val currentList = _basicProducts.value.toMutableList()
                                    currentList.addAll(newProducts)
                                    _basicProducts.value = currentList
                                    currentPage++
                                }
                            }
                        }
                        .onFailure { exception ->
                            withContext(Dispatchers.Main) {
                                _error.value = exception.message
                            }
                        }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _loading.value = false
                    _isLoadingMore.value = false
                }
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