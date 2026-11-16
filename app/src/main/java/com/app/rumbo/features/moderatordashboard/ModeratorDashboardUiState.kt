package com.app.rumbo.features.moderatordashboard

import com.app.rumbo.domain.model.ModerationActivity
import com.app.rumbo.domain.model.ModeratorStats

data class ModeratorDashboardUiState(
    val userName: String = "",
    val userRole: String = "MODERADOR",
    val stats: ModeratorStats = ModeratorStats(),
    val recentActivity: List<ModerationActivity> = emptyList(),
    val isLoading: Boolean = false
)
