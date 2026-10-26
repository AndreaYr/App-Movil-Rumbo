package com.app.rumbo.features.pendingpublications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.usecase.GetPendingPublicationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PendingPublicationsViewModel @Inject constructor(
    private val getPendingPublications: GetPendingPublicationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingPublicationsUiState())
    val uiState: StateFlow<PendingPublicationsUiState> = _uiState.asStateFlow()

    init {
        loadPendingPublications()
    }

    private fun loadPendingPublications() {
        viewModelScope.launch {
            getPendingPublications().collect { pendingList ->
                _uiState.value = _uiState.value.copy(
                    publications = pendingList,
                    pendingCount = pendingList.size,
                    isLoading = false
                )
            }
        }
    }
}
