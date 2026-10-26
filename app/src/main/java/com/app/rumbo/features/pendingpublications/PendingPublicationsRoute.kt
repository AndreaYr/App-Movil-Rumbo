package com.app.rumbo.features.pendingpublications

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PendingPublicationsRoute(
    onBack: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onVerifyClick: (String) -> Unit = {},
    onRejectClick: (String) -> Unit = {},
    onNavigateToPanel: () -> Unit = {},
    onNavigateToPending: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {},
    viewModel: PendingPublicationsViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    
    PendingPublicationsScreen(
        state = state.value,
        onBack = onBack,
        onFilterClick = onFilterClick,
        onVerifyClick = onVerifyClick,
        onRejectClick = onRejectClick,
        onNavigateToPanel = onNavigateToPanel,
        onNavigateToPending = onNavigateToPending,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToAccount = onNavigateToAccount
    )
}
