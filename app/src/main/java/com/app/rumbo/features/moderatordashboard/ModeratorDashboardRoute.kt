package com.app.rumbo.features.moderatordashboard

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ModeratorDashboardRoute(
    onStartReview: () -> Unit = {},
    onNavigateToPending: () -> Unit = {},
    onNavigateToVerified: () -> Unit = {},
    onNavigateToRejected: () -> Unit = {},
    onNavigateToFinished: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ModeratorDashboardViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    
    ModeratorDashboardScreen(
        state = state.value,
        onStartReview = onStartReview,
        onNavigateToPending = onNavigateToPending,
        onNavigateToVerified = onNavigateToVerified,
        onNavigateToRejected = onNavigateToRejected,
        onNavigateToFinished = onNavigateToFinished,
        onNavigateToHistory = onNavigateToHistory,
        onNavigateToAccount = onNavigateToAccount,
        onLogout = onLogout
    )
}
