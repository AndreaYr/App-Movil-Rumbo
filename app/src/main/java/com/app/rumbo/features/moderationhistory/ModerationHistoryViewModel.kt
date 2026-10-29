package com.app.rumbo.features.moderationhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.usecase.GetModerationHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class ModerationHistoryViewModel @Inject constructor(
    private val getModerationHistory: GetModerationHistoryUseCase
) : ViewModel() {

    private val selectedFilter = MutableStateFlow(ModerationHistoryFilter.ALL)
    private val _uiState = MutableStateFlow(ModerationHistoryUiState())
    val uiState: StateFlow<ModerationHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun onFilterSelected(filter: ModerationHistoryFilter) {
        selectedFilter.value = filter
    }

    private fun loadHistory() {
        viewModelScope.launch {
            combine(getModerationHistory(), selectedFilter) { history, filter ->
                val visiblePublications = when (filter) {
                    ModerationHistoryFilter.ALL -> history
                    ModerationHistoryFilter.APPROVED ->
                        history.filter { it.status == PublicationStatus.VERIFIED }
                    ModerationHistoryFilter.REJECTED ->
                        history.filter { it.status == PublicationStatus.REJECTED }
                }
                ModerationHistoryUiState(
                    publications = visiblePublications,
                    filter = filter,
                    approvedCount = history.count { it.status == PublicationStatus.VERIFIED },
                    rejectedCount = history.count { it.status == PublicationStatus.REJECTED },
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
