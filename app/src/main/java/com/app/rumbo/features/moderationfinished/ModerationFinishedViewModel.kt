package com.app.rumbo.features.moderationfinished

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.app.rumbo.navigation.ModerationFinishedDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ModerationFinishedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val route = savedStateHandle.toRoute<ModerationFinishedDestination>()

    private val _uiState = MutableStateFlow(ModerationFinishedUiState(route.outcome))
    val uiState: StateFlow<ModerationFinishedUiState> = _uiState.asStateFlow()
}
