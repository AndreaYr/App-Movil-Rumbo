package com.app.rumbo.features.moderatordashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.usecase.GetCurrentUserUseCase
import com.app.rumbo.domain.usecase.GetModeratorStatsUseCase
import com.app.rumbo.domain.usecase.GetRecentModerationActivityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ModeratorDashboardViewModel @Inject constructor(
    private val getModeratorStatsUseCase: GetModeratorStatsUseCase,
    private val getRecentModerationActivityUseCase: GetRecentModerationActivityUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModeratorDashboardUiState())
    val uiState: StateFlow<ModeratorDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            combine(
                getModeratorStatsUseCase(),
                getRecentModerationActivityUseCase(),
                getCurrentUserUseCase()
            ) { stats, activities, currentUser ->
                _uiState.update { state ->
                    state.copy(
                        stats = stats,
                        recentActivity = activities,
                        userName = currentUser?.name ?: state.userName,
                        isLoading = false
                    )
                }
            }.collect { }
        }
    }
}
