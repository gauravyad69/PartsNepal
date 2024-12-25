package np.com.parts.Domain.ViewModels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import np.com.parts.API.Repository.MiscRepository
import np.com.parts.API.Repository.Paste
import javax.inject.Inject

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class HomePageCarrousel(
    val imageUrl: String,
    val title: String,
    val caption: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class DialogContent(
    val title: String,
    val imageUrl: String,
    val description: String,
    val setCancelable: Boolean
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Content(
    val content: String
)



@HiltViewModel
class MiscViewModel @Inject constructor(private val miscRepository: MiscRepository
) : ViewModel() {




    private val _miscList = MutableStateFlow<List<Paste>>(emptyList())
    val miscList: StateFlow<List<Paste>> = _miscList.asStateFlow()

    private val _homePageCarousel = MutableStateFlow<List<HomePageCarrousel>>(emptyList())
    val homePageCarrousel: StateFlow<List<HomePageCarrousel>> = _homePageCarousel.asStateFlow()

    private val _dialogContent = MutableStateFlow<DialogContent?>(null)
    val dialogContent: StateFlow<DialogContent?> = _dialogContent.asStateFlow()

  private val _privacyPolicy = MutableStateFlow<Content?>(null)
    val privacyPolicy: StateFlow<Content?> = _privacyPolicy.asStateFlow()

 private val _termsAndCondition = MutableStateFlow<Content?>(null)
    val termsAndCondition: StateFlow<Content?> = _termsAndCondition.asStateFlow()


    private val _pasteById = MutableStateFlow<Paste?>(null)
    val pasteById: StateFlow<Paste?> = _pasteById.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()



    fun loadMiscDetails(page: Int = 0) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            miscRepository.getAllPastes()
                .onSuccess { response ->
                    _miscList.value = response
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }

            _loading.value = false
        }
    }

    fun loadMiscById(id: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            miscRepository.getPasteById(id)
                .onSuccess { response ->
                    _pasteById.value = response
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }

            _loading.value = false
        }
    }

    suspend fun loadHomePageCarrousel(){
        val id="e55b161a-51b7-4f4c-b991-e54e838f95fb"
        _loading.value=true
        miscRepository.getPasteById(id)
            .onSuccess { response ->
                val parsed_data = Json.decodeFromString<List<HomePageCarrousel>>(response.content)
                _homePageCarousel.value=parsed_data
            }
            .onFailure { exception ->
                _error.value = exception.message
            }
        _loading.value=false
    }

    suspend fun loadTermsAndConditions(){
        val id="this_is_where_termsandconditions_id_goes"
        _loading.value=true
        miscRepository.getPasteById(id)
            .onSuccess { response ->
                val parsed_data = Json.decodeFromString<Content>(response.content)
                _termsAndCondition.value=parsed_data
            }
            .onFailure { exception ->
                _error.value = exception.message
            }
        _loading.value=false
    }
    suspend fun loadPrivacyPolicy(){
        val id="this_is_where_privacy_id_goes"
        _loading.value=true
        miscRepository.getPasteById(id)
            .onSuccess { response ->
                val parsed_data = Json.decodeFromString<Content>(response.content)
                _privacyPolicy.value=parsed_data
            }
            .onFailure { exception ->
                _error.value = exception.message
            }
        _loading.value=false
    }


    suspend fun loadVoucherDialog(){
        val id="this_is_where_dialog_id_goes"
        _loading.value=true
        miscRepository.getPasteById(id)
            .onSuccess { response ->
                val parsed_data = Json.decodeFromString<DialogContent>(response.content)
                _dialogContent.value=parsed_data
            }
            .onFailure { exception ->
                _error.value = exception.message
            }
        _loading.value=false
    }




    // Similar functions for other API calls...
}



