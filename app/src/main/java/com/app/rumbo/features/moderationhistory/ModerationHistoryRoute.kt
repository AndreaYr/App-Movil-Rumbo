package com.app.rumbo.features.moderationhistory

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ModerationHistoryRoute(
    onBack: () -> Unit = {},
    onPublicationClick: (String) -> Unit = {},
    onNavigateToPanel: () -> Unit = {},
    onNavigateToPending: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {},
    viewModel: ModerationHistoryViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()

    ModerationHistoryScreen(
        state = state.value,
        onBack = onBack,
        onFilterSelected = viewModel::onFilterSelected,
        onPublicationClick = onPublicationClick,
        onNavigateToPanel = onNavigateToPanel,
        onNavigateToPending = onNavigateToPending,
        onNavigateToAccount = onNavigateToAccount
    )
}
