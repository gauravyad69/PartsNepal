package np.com.parts.Domain.ViewModels

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import np.com.parts.API.Repository.ProductRepository
import np.com.parts.API.Models.BasicProductView
import np.com.parts.API.Models.ProductModel
import np.com.parts.Presentation.Adapter.CarouselImage
import np.com.parts.Presentation.Adapter.Deal
import np.com.parts.Presentation.Models.HomeItem
import timber.log.Timber
import javax.inject.Inject
import kotlinx.coroutines.async
import np.com.parts.API.Models.BasicProductInfo
import np.com.parts.API.Models.Discount
import np.com.parts.API.Models.DiscountType
import np.com.parts.API.Models.InventoryInfo
import np.com.parts.API.Models.Money
import np.com.parts.API.Models.PricingInfo
import np.com.parts.API.Models.toBasicProductView
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

@HiltViewModel
class ProductViewModel @Inject constructor(    private val productRepository: ProductRepository
) : ViewModel() {
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

    private val _carouselImages = MutableStateFlow<List<CarouselImage>>(emptyList())
    val carouselImages = _carouselImages.asStateFlow()

    private val _deals = MutableStateFlow<List<Deal>>(emptyList())
    val deals = _deals.asStateFlow()


    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchResults = MutableStateFlow<List<ProductModel>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    // New state for combined home items
    private val _homeItems = MutableStateFlow<List<HomeItem>>(emptyList())
    val homeItems = _homeItems.asStateFlow()

    // New state for hot deals loading
    private val _isLoadingHotDeals = MutableStateFlow(false)
    val isLoadingHotDeals = _isLoadingHotDeals.asStateFlow()

    fun saveScrollState(state: Parcelable) {
        recyclerViewState = state
    }

    fun getScrollState(): Parcelable? = recyclerViewState

    init {
        loadInitialHomeData()
    }

    private fun loadInitialHomeData() {
        viewModelScope.launch {
            try {
                _loading.value = true
                
                // Load banner and hot deals in parallel
                val deferredBanner = async { productRepository.getCarouselImages() }
                val deferredHotDeals = async { productRepository.getDeals() }
                val deferredBasicProducts = async { 
                    productRepository.getBasicProducts(1, pageSize)
                }

                // Collect results
                val bannerResult = deferredBanner.await()
                val hotDealsResult = deferredHotDeals.await()
                val basicProductsResult = deferredBasicProducts.await()

                // Create home items list
                val homeItemsList = mutableListOf<HomeItem>()

                // Add banner if successful
                bannerResult.onSuccess { carouselImages ->
                    if (carouselImages.isNotEmpty()) {
                        homeItemsList.add(HomeItem.BannerItem(carouselImages.map { 
                            CarouselItem(it.imageUrl)
                        }))
                    }
                }.onFailure { 
                    _error.value = "Failed to load banner"
                }

                // Add hot deals if successful
                hotDealsResult.onSuccess { deals ->
                    if (deals.isNotEmpty()) {
                        homeItemsList.add(HomeItem.HotDealsSection(deals.map {
                            // Convert Deal to BasicProductView if needed
                            // This depends on your data structure
                            it.toBasicProductView()
                        }))
                    }
                }.onFailure {
                    _error.value = "Failed to load hot deals"
                }

                // Add basic products if successful
                basicProductsResult.onSuccess { response ->
                    response.data.forEach { product ->
                        homeItemsList.add(HomeItem.RegularProduct(product))
                    }
                    if (response.data.isNotEmpty()) {
                        currentPage++
                    }
                }.onFailure {
                    _error.value = "Failed to load products"
                }

                _homeItems.value = homeItemsList

            } finally {
                _loading.value = false
            }
        }
    }

    // Modified to work with HomeItems
    fun loadMoreProducts() {
        if (isLastPage || _isLoadingMore.value) return

        viewModelScope.launch {
            try {
                _isLoadingMore.value = true

                productRepository.getBasicProducts(currentPage, pageSize)
                    .onSuccess { response ->
                        val newProducts = response.data.map { HomeItem.RegularProduct(it) }
                        if (newProducts.isEmpty()) {
                            isLastPage = true
                        } else {
                            val currentList = _homeItems.value.toMutableList()
                            currentList.addAll(newProducts)
                            _homeItems.value = currentList
                            currentPage++
                        }
                    }
                    .onFailure { exception ->
                        _error.value = exception.message
                    }
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    // Modified reset function
    fun resetHomeData() {
        currentPage = 1
        isLastPage = false
        _homeItems.value = emptyList()
        loadInitialHomeData()
    }

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

                // Load carousel images
                productRepository.getCarouselImages()
                    .onSuccess { _carouselImages.value = it }
                    .onFailure { /* Handle error */ _error.value = "FAILURE AT CAROUSEL IMAGE"}

                // Load deals
                productRepository.getDeals()
                    .onSuccess { _deals.value = it }
                    .onFailure { /* Handle error */ _error.value = "FAILURE AT DEALS REPOSITORY" }



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



    fun sendReview(rating: Int, review: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val productId = productById.value!!.productId
            productRepository.sendReview(productId, rating, review)
                .onSuccess { response ->
                    Timber.log(1,"Successfully added review")
                }
                .onFailure { exception ->
                    _error.value = exception.message
                    Timber.log(1,"Failed ${exception.message}")
                }

            _loading.value = false
        }
    }


    // Similar functions for other API calls...
}