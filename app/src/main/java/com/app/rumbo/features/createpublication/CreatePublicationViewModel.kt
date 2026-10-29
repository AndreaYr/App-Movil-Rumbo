package com.app.rumbo.features.createpublication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.data.local.CurrentUser
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.Photo
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.PublicationDraft
import com.app.rumbo.domain.usecase.PublishPublicationUseCase
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

/** Centro aproximado del Quindío: punto de partida del marcador del mapa. */
private const val DEFAULT_LATITUDE = 4.5871
private const val DEFAULT_LONGITUDE = -75.6429

data class CreatePublicationUiState(
    val photos: List<Photo> = emptyList(),
    val title: String = "",
    val category: Category = Category.NATURE,
    val description: String = "",
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

    val titleError: String? get() = if (showValidationErrors && title.isBlank()) "Escribe un título" else null
    val descriptionError: String? get() = if (showValidationErrors && description.isBlank()) "Cuenta algo sobre el lugar" else null

    val isValid: Boolean get() = title.isNotBlank() && description.isNotBlank()
}

/** Avisos de una sola vez que la pantalla debe atender (navegar, mostrar mensaje). */
sealed interface CreatePublicationEvent {
    data object DraftSaved : CreatePublicationEvent
    data class Published(val publicationId: String) : CreatePublicationEvent
}

@HiltViewModel
class CreatePublicationViewModel @Inject constructor(
    private val publishPublication: PublishPublicationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePublicationUiState())
    val uiState: StateFlow<CreatePublicationUiState> = _uiState.asStateFlow()

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

    fun onScheduleChange(value: String) = _uiState.update { it.copy(schedule = value) }

    fun onPriceSelected(price: PriceLevel) = _uiState.update { it.copy(price = price) }

    /** El marcador del mapa se movió: se actualizan las coordenadas de la publicación. */
    fun onMarkerMoved(latitude: Double, longitude: Double) = _uiState.update {
        it.copy(latitude = latitude, longitude = longitude)
    }

    fun onUseMyLocation() {
        // TODO: reemplazar por la ubicación real (FusedLocationProviderClient) cuando se pida permiso.
        onMarkerMoved(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
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
    schedule = schedule.trim(),
    price = price,
    latitude = latitude,
    longitude = longitude,
    photos = photos
)
