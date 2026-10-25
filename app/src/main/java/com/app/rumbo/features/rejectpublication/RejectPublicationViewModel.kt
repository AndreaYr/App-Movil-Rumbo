package com.app.rumbo.features.rejectpublication

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.rumbo.domain.repository.PublicationRepository
import com.app.rumbo.navigation.RejectPublicationDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RejectPublicationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val publicationRepository: PublicationRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<RejectPublicationDestination>()
    private val publicationId = route.publicationId

    private val _uiState = MutableStateFlow(RejectPublicationUiState())
    val uiState: StateFlow<RejectPublicationUiState> = _uiState.asStateFlow()

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

    fun selectReason(reason: String) {
        _uiState.update { it.copy(selectedReason = reason) }
    }

    fun updateDetail(detail: String) {
        val limitedDetail = detail.take(300)
        _uiState.update {
            it.copy(
                detail = limitedDetail,
                charCount = limitedDetail.length
            )
        }
    }
}
