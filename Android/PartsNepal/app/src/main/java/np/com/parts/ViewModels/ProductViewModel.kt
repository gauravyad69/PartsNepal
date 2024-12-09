package np.com.parts.ViewModels

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import np.com.parts.API.Repository.ProductRepository
import np.com.parts.API.Models.BasicProductView
import np.com.parts.API.Models.ProductModel

class ProductViewModel : ViewModel() {
    private val productRepository=ProductRepository()
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

    private val _searchResults = MutableStateFlow<List<ProductModel>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    fun saveScrollState(state: Parcelable) {
        recyclerViewState = state
    }

    fun getScrollState(): Parcelable? = recyclerViewState

    fun loadProducts(page: Int = 0) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            productRepository.getAllProducts(page = page)
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

            productRepository.getProductById(id)
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
                    productRepository.getBasicProducts(currentPage, pageSize)
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

    fun searchProducts(query: String, page: Int = 1, onSale: Boolean? = null) {
        if (_isSearching.value) return

        viewModelScope.launch {
            try {
                _isSearching.value = true
                _error.value = null

                productRepository.searchProducts(
                    query = query,
                    page = page - 1,
                    pageSize = pageSize,
                    onSale = onSale
                ).onSuccess { response ->
                    if (page == 1) {
                        _searchResults.value = response.data
                    } else {
                        _searchResults.value = _searchResults.value + response.data
                    }
                    // Update pagination state based on metadata
                    response.metadata?.let { metadata ->
                        isLastPage = (metadata.page!! + 1) * metadata.itemsPerPage!! >= metadata.totalItems!!
                    }
                }.onFailure { exception ->
                    _error.value = exception.message
                }
            } finally {
                _isSearching.value = false
            }
        }
    }

    // Similar functions for other API calls...
}