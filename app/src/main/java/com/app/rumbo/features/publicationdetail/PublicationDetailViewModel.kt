package com.app.rumbo.features.publicationdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.data.local.CurrentUser
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.usecase.AddCommentUseCase
import com.app.rumbo.domain.usecase.MAX_COMMENT_LENGTH
import com.app.rumbo.domain.usecase.ToggleCommentLikeUseCase
import com.app.rumbo.domain.usecase.GetPublicationDetailUseCase
import com.app.rumbo.domain.usecase.ToggleInterestUseCase
import com.app.rumbo.domain.usecase.ToggleVisitedUseCase
import com.app.rumbo.navigation.PublicationDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val toggleVisited: ToggleVisitedUseCase,
    private val addComment: AddCommentUseCase,
    private val toggleCommentLike: ToggleCommentLikeUseCase
) : ViewModel() {

    // Navigation guarda los argumentos de la ruta con el nombre de cada propiedad
    private val publicationId: String =
        checkNotNull(savedStateHandle[PublicationDetailDestination.ARG_ID]) { "Falta el id de la publicación" }

    val uiState: StateFlow<PublicationDetailUiState> = getPublicationDetail(publicationId)
        .map { publication ->
            if (publication == null) PublicationDetailUiState.NotFound else PublicationDetailUiState.Success(publication)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PublicationDetailUiState.Loading)

    private val _commentText = MutableStateFlow("")
    /** Texto que el usuario está escribiendo en el campo de comentario. */
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    fun onCommentChange(text: String) {
        _commentText.value = text.take(MAX_COMMENT_LENGTH)
    }

    /** Publica el comentario y limpia el campo solo si quedó guardado. */
    fun onSendComment() {
        val text = _commentText.value
        viewModelScope.launch {
            if (addComment(publicationId, text, CurrentUser.author)) _commentText.value = ""
        }
    }

    fun onToggleCommentLike(commentId: String) {
        viewModelScope.launch { toggleCommentLike(publicationId, commentId) }
    }

    fun onToggleInterest() {
        viewModelScope.launch { toggleInterest(publicationId) }
    }

    fun onToggleVisited() {
        viewModelScope.launch { toggleVisited(publicationId) }
    }
}
