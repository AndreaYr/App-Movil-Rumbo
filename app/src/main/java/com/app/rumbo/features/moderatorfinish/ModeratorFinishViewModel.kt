package com.app.rumbo.features.moderatorfinish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.usecase.FinishPublicationUseCase
import com.app.rumbo.domain.usecase.GetFinishablePublicationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModeratorFinishViewModel @Inject constructor(
    getFinishablePublications: GetFinishablePublicationsUseCase,
    private val finishPublication: FinishPublicationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModeratorFinishUiState())
    val uiState: StateFlow<ModeratorFinishUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getFinishablePublications().collect { publications ->
                _uiState.update { it.copy(isLoading = false, publication = publications.firstOrNull()) }
            }
        }
    }

    /** Finaliza la publicación mostrada y avisa a la pantalla cuando ya quedó guardado. */
    fun onConfirm(onDone: () -> Unit) {
        val state = _uiState.value
        val publication = state.publication ?: return
        if (state.isFinishing) return
        _uiState.update { it.copy(isFinishing = true) }
        viewModelScope.launch {
            val finished = finishPublication(publication.id)
            _uiState.update { it.copy(isFinishing = false) }
            if (finished) onDone()
        }
    }
}
