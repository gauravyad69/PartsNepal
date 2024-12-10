package np.com.parts.ViewModels

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
import np.com.parts.API.Repository.MiscRepository
import javax.inject.Inject

@HiltViewModel
class MiscViewModel @Inject constructor(private val miscRepository: MiscRepository
) : ViewModel() {
    private var recyclerViewState: Parcelable? = null
    private var currentPage = 1
    private var isLastPage = false
    private val pageSize = 10




    private val _misc = MutableStateFlow<List<MiscModel>>(emptyList())
    val misc: StateFlow<List<ProductModel>> = _misc.asStateFlow()

//   private val _productById = MutableStateFlow<ProductModel?>(null)
//    val productById: StateFlow<ProductModel?> = _productById.asStateFlow()
//
//    private val _basicProducts = MutableStateFlow<List<BasicProductView>>(emptyList())
//    val basicProducts = _basicProducts.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()






    fun loadMiscDetails(page: Int = 0) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            miscRepository.getAllMiscs()
                .onSuccess { response ->
                    _misc.value = response.data
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }

            _loading.value = false
        }
    }

    fun loadMiscById(id: Int = 0) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            miscRepository.getProductById(id)
                .onSuccess { response ->
                    _misc.value = response.data
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }

            _loading.value = false
        }
    }

    // Similar functions for other API calls...
}