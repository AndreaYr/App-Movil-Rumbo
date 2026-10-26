package com.app.rumbo.features.reviewpublication

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.rumbo.domain.repository.PublicationRepository
import com.app.rumbo.navigation.ReviewPublicationDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ReviewPublicationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val publicationRepository: PublicationRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<ReviewPublicationDestination>()
    private val publicationId = route.publicationId

    private val _uiState = MutableStateFlow(ReviewPublicationUiState())
    val uiState: StateFlow<ReviewPublicationUiState> = _uiState.asStateFlow()

    init {
        loadPublication()
    }

    private fun loadPublication() {
        viewModelScope.launch {
            publicationRepository.observePublication(publicationId).collect { publication ->
                _uiState.update { it.copy(publication = publication) }
            }
        }
    }
}
