package com.app.rumbo.features.moderationfinished

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ModerationFinishedRoute(
    onBackToPanel: () -> Unit = {},
    title: String? = null,
    message: String? = null,
    viewModel: ModerationFinishedViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    val finalState = if (title != null || message != null) {
        state.value.copy(
            title = title ?: state.value.title,
            message = message ?: state.value.message
        )
    } else {
        state.value
    }
    ModerationFinishedScreen(state = finalState, onBackToPanel = onBackToPanel)
}
