package com.app.rumbo.features.editpublication

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.PublicationDraft
import com.app.rumbo.domain.usecase.GetPublicationDetailUseCase
import com.app.rumbo.domain.usecase.ResubmitPublicationUseCase
import com.app.rumbo.features.createpublication.MAX_DESCRIPTION_LENGTH
import com.app.rumbo.navigation.EditPublicationDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Corrección de una publicación rechazada: se cargan sus datos una vez y se reenvía a revisión. */
@HiltViewModel
class EditPublicationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getPublicationDetail: GetPublicationDetailUseCase,
    private val resubmitPublication: ResubmitPublicationUseCase
) : ViewModel() {

    private val publicationId = savedStateHandle.toRoute<EditPublicationDestination>().publicationId

    private val _uiState = MutableStateFlow(EditPublicationUiState())
    val uiState: StateFlow<EditPublicationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val publication = getPublicationDetail(publicationId).first()
            _uiState.update {
                if (publication == null) {
                    it.copy(isLoading = false)
                } else {
                    it.copy(
                        isLoading = false,
                        status = publication.status,
                        createdAt = publication.publishedAt,
                        rejectionReason = publication.rejectionReason,
                        rejectionDetail = publication.rejectionDetail,
                        photos = publication.photos,
                        title = publication.title,
                        category = publication.category,
                        description = publication.description,
                        schedule = publication.schedule,
                        price = publication.price,
                        latitude = publication.latitude,
                        longitude = publication.longitude
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }

    fun onCategorySelected(category: Category) = _uiState.update { it.copy(category = category) }

    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value.take(MAX_DESCRIPTION_LENGTH)) }

    fun onScheduleChange(value: String) = _uiState.update { it.copy(schedule = value) }

    fun onPriceSelected(price: PriceLevel) = _uiState.update { it.copy(price = price) }

    fun onMarkerMoved(latitude: Double, longitude: Double) = _uiState.update {
        it.copy(latitude = latitude, longitude = longitude)
    }

    /** Valida y reenvía; avisa a la pantalla cuando la publicación ya volvió a la cola del moderador. */
    fun onResubmit(onDone: () -> Unit) {
        val state = _uiState.value
        if (state.isSaving) return
        if (!state.isValid) {
            _uiState.update { it.copy(showValidationErrors = true) }
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val resubmitted = resubmitPublication(publicationId, state.toDraft())
            _uiState.update { it.copy(isSaving = false) }
            if (resubmitted) onDone()
        }
    }
}

private fun EditPublicationUiState.toDraft() = PublicationDraft(
    title = title.trim(),
    description = description.trim(),
    category = category,
    schedule = schedule.trim(),
    price = price,
    latitude = latitude,
    longitude = longitude,
    photos = photos
)
