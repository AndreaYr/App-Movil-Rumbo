package com.app.rumbo.features.publicationdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.usecase.GetPublicationDetailUseCase
import com.app.rumbo.domain.usecase.ToggleInterestUseCase
import com.app.rumbo.domain.usecase.ToggleVisitedUseCase
import com.app.rumbo.navigation.PublicationDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PublicationDetailUiState {
    data object Loading : PublicationDetailUiState
    data object NotFound : PublicationDetailUiState
    data class Success(val publication: Publication) : PublicationDetailUiState
}

@HiltViewModel
class PublicationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getPublicationDetail: GetPublicationDetailUseCase,
    private val toggleInterest: ToggleInterestUseCase,
    private val toggleVisited: ToggleVisitedUseCase
) : ViewModel() {

    // Navigation guarda los argumentos de la ruta con el nombre de cada propiedad
    private val publicationId: String =
        checkNotNull(savedStateHandle[PublicationDetailDestination.ARG_ID]) { "Falta el id de la publicación" }

    val uiState: StateFlow<PublicationDetailUiState> = getPublicationDetail(publicationId)
        .map { publication ->
            if (publication == null) PublicationDetailUiState.NotFound else PublicationDetailUiState.Success(publication)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PublicationDetailUiState.Loading)

    fun onToggleInterest() {
        viewModelScope.launch { toggleInterest(publicationId) }
    }

    fun onToggleVisited() {
        viewModelScope.launch { toggleVisited(publicationId) }
    }
}
