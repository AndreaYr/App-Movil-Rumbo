package com.app.rumbo.features.moderationfinished

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ModerationFinishedViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ModerationFinishedUiState())
    val uiState: StateFlow<ModerationFinishedUiState> = _uiState.asStateFlow()
}
