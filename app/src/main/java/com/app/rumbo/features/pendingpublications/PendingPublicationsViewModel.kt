package com.app.rumbo.features.pendingpublications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.PublicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PendingPublicationsViewModel @Inject constructor(
    private val publicationRepository: PublicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingPublicationsUiState())
    val uiState: StateFlow<PendingPublicationsUiState> = _uiState.asStateFlow()

    init {
        loadPendingPublications()
    }

    private fun loadPendingPublications() {
        viewModelScope.launch {
            publicationRepository.observePublications()
                .map { publications ->
                    publications
                        .filter { it.status == PublicationStatus.PENDING }
                        .sortedBy { it.publishedAt }
                }
                .collect { pendingList ->
                    _uiState.update {
                        it.copy(
                            publications = pendingList,
                            pendingCount = pendingList.size,
                            isLoading = false
                        )
                    }
                }
        }
    }
}
