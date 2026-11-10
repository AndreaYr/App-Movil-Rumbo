package com.app.rumbo.features.createpublication

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.R
import com.app.rumbo.data.local.CurrentUser
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.Photo
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.PublicationDraft
import com.app.rumbo.domain.model.PlaceResult
import com.app.rumbo.domain.usecase.PublishPublicationUseCase
import com.app.rumbo.domain.usecase.ReverseGeocodeUseCase
import com.app.rumbo.domain.usecase.SearchPlacesUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val MAX_PHOTOS = 6
const val MAX_DESCRIPTION_LENGTH = 500
private const val ADDRESS_SEARCH_DELAY_MS = 400L

/** Centro aproximado del Quindío: punto de partida del marcador del mapa. */
private const val DEFAULT_LATITUDE = 4.5871
private const val DEFAULT_LONGITUDE = -75.6429

data class CreatePublicationUiState(
    val photos: List<Photo> = emptyList(),
    val title: String = "",
    val category: Category = Category.NATURE,
    val description: String = "",
    val department: String? = null,
    val town: String? = null,
    val address: String = "",
    val addressSuggestions: List<PlaceResult> = emptyList(),
    /** Punto al que debe volar la cámara tras elegir una dirección; null = no mover la cámara. */
    val focusLatitude: Double? = null,
    val focusLongitude: Double? = null,
    val schedule: String = "7:00 - 18:00",
    val price: PriceLevel = PriceLevel.CHEAP,
    val latitude: Double = DEFAULT_LATITUDE,
    val longitude: Double = DEFAULT_LONGITUDE,
    val isSaving: Boolean = false,
    val showValidationErrors: Boolean = false
) {
    val remainingPhotoSlots: Int get() = (MAX_PHOTOS - photos.size).coerceAtLeast(0)
    val canAddPhoto: Boolean get() = photos.size < MAX_PHOTOS
    val descriptionLength: Int get() = description.length

    @get:StringRes
    val titleError: Int? get() = if (showValidationErrors && title.isBlank()) {
        R.string.create_title_required
    } else null

    @get:StringRes
    val descriptionError: Int? get() = if (showValidationErrors && description.isBlank()) {
        R.string.create_description_required
    } else null

    val isValid: Boolean get() = title.isNotBlank() && description.isNotBlank()
}

/** Avisos de una sola vez que la pantalla debe atender (navegar, mostrar mensaje). */
sealed interface CreatePublicationEvent {
    data object DraftSaved : CreatePublicationEvent
    data class Published(val publicationId: String) : CreatePublicationEvent
}

@HiltViewModel
class CreatePublicationViewModel @Inject constructor(
    private val publishPublication: PublishPublicationUseCase,
    private val searchPlaces: SearchPlacesUseCase,
    private val reverseGeocode: ReverseGeocodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePublicationUiState())
    val uiState: StateFlow<CreatePublicationUiState> = _uiState.asStateFlow()

    private val addressQuery = MutableStateFlow("")
    private var reverseJob: Job? = null

    init {
        // Espera a que termine de escribir y descarta las búsquedas que ya no valen
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            addressQuery.debounce(ADDRESS_SEARCH_DELAY_MS).distinctUntilChanged().collectLatest { query ->
                val state = _uiState.value
                val results = searchPlaces(query, state.latitude, state.longitude)
                _uiState.update { it.copy(addressSuggestions = results) }
            }
        }
    }

    private val _events = Channel<CreatePublicationEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onPhotosAdded(uris: List<String>) {
        if (uris.isEmpty()) return
        _uiState.update { state ->
            val newPhotos = uris.take(state.remainingPhotoSlots).mapIndexed { index, uri ->
                Photo(url = uri, description = "Foto ${state.photos.size + index + 1}")
            }
            if (newPhotos.isEmpty()) state else state.copy(photos = state.photos + newPhotos)
        }
    }

    fun onRemovePhoto(index: Int) {
        _uiState.update { state ->
            if (index in state.photos.indices) {
                state.copy(photos = state.photos.filterIndexed { i, _ -> i != index })
            } else {
                state
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }

    fun onCategorySelected(category: Category) = _uiState.update { it.copy(category = category) }

    fun onDescriptionChange(value: String) = _uiState.update {
        it.copy(description = value.take(MAX_DESCRIPTION_LENGTH))
    }

    /** Al cambiar de departamento, el municipio elegido deja de valer. */
    fun onDepartmentSelected(value: String?) = _uiState.update { it.copy(department = value, town = null) }

    fun onTownSelected(value: String?) = _uiState.update { it.copy(town = value) }

    fun onScheduleChange(value: String) = _uiState.update { it.copy(schedule = value) }

    fun onPriceSelected(price: PriceLevel) = _uiState.update { it.copy(price = price) }

    /** El usuario escribe la dirección: se buscan coincidencias reales con Mapbox. */
    fun onAddressChange(value: String) {
        _uiState.update { it.copy(address = value, addressSuggestions = if (value.isBlank()) emptyList() else it.addressSuggestions) }
        addressQuery.value = value
    }

    /** Elegir una sugerencia pone el marcador en el punto exacto y lleva la cámara hasta allí. */
    fun onAddressSuggestionSelected(place: PlaceResult) {
        reverseJob?.cancel()
        addressQuery.value = ""
        _uiState.update {
            it.copy(
                address = place.fullAddress,
                addressSuggestions = emptyList(),
                latitude = place.latitude,
                longitude = place.longitude,
                focusLatitude = place.latitude,
                focusLongitude = place.longitude
            )
        }
    }

    /**
     * El marcador del mapa se movió (toque, arrastre o "Usar mi ubicación"): se actualizan las coordenadas
     * y se detecta la dirección de ese punto.
     */
    fun onMarkerMoved(latitude: Double, longitude: Double) {
        _uiState.update { it.copy(latitude = latitude, longitude = longitude, addressSuggestions = emptyList()) }
        addressQuery.value = ""
        reverseJob?.cancel()
        reverseJob = viewModelScope.launch {
            reverseGeocode(latitude, longitude)?.let { place ->
                _uiState.update { it.copy(address = place.fullAddress) }
            }
        }
    }

    /** Guarda un borrador local mientras la pantalla siga abierta. */
    fun onSaveDraft() {
        viewModelScope.launch { _events.send(CreatePublicationEvent.DraftSaved) }
    }

    /** Valida y envía la publicación para revisión del moderador. */
    fun onPublish() {
        val state = _uiState.value
        if (!state.isValid) {
            _uiState.update { it.copy(showValidationErrors = true) }
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val publication = publishPublication(state.toDraft(), CurrentUser.author)
            _uiState.update { it.copy(isSaving = false) }
            _events.send(CreatePublicationEvent.Published(publication.id))
        }
    }
}

private fun CreatePublicationUiState.toDraft() = PublicationDraft(
    title = title.trim(),
    description = description.trim(),
    category = category,
    address = address.trim(),
    department = department.orEmpty(),
    town = town.orEmpty(),
    schedule = schedule.trim(),
    price = price,
    latitude = latitude,
    longitude = longitude,
    photos = photos
)
