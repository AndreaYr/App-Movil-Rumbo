package com.app.rumbo.features.reviewpublication

import kotlinx.coroutines.CancellationException
import com.app.rumbo.R
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.rumbo.domain.usecase.GetPublicationDetailUseCase
import com.app.rumbo.domain.usecase.ApprovePublicationUseCase
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
    private val getPublicationDetail: GetPublicationDetailUseCase,
    private val approvePublication: ApprovePublicationUseCase
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
            getPublicationDetail(publicationId).collect { publication ->
                _uiState.update { it.copy(publication = publication) }
            }
        }
    }

    /** Aprueba la publicación y avisa a la pantalla cuando ya quedó guardado. */
    fun onVerify(onDone: () -> Unit) {
        if (_uiState.value.isProcessing) return
        _uiState.update { it.copy(isProcessing = true, errorRes = null) }
        viewModelScope.launch {
            try {
                if (approvePublication(publicationId)) {
                    onDone()
                } else {
                    // Otro moderador ya la resolvió (o ya no existe)
                    _uiState.update { it.copy(errorRes = R.string.moderation_error_already_resolved) }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update { it.copy(errorRes = R.string.moderation_error_connection) }
            } finally {
                _uiState.update { it.copy(isProcessing = false) }
            }
        }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorRes = null) }
    }
}
