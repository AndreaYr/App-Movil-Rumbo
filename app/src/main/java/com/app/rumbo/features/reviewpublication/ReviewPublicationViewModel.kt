package com.app.rumbo.features.reviewpublication

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.rumbo.domain.usecase.ApprovePublicationUseCase
import com.app.rumbo.domain.usecase.GetPublicationDetailUseCase
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

    fun approve() {
        if (_uiState.value.isProcessing || _uiState.value.moderationCompleted) return
        _uiState.update { it.copy(isProcessing = true, actionFailed = false) }
        viewModelScope.launch {
            val approved = approvePublication(publicationId)
            _uiState.update {
                it.copy(
                    isProcessing = false,
                    actionFailed = !approved,
                    moderationCompleted = approved
                )
            }
        }
    }
}
