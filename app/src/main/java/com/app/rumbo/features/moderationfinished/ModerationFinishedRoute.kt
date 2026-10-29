package com.app.rumbo.features.moderationfinished

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ModerationFinishedRoute(
    onBackToPanel: () -> Unit = {},
    viewModel: ModerationFinishedViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    ModerationFinishedScreen(state = state.value, onBackToPanel = onBackToPanel)
}
