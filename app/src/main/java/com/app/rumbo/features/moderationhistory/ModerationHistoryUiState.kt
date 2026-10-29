package com.app.rumbo.features.moderationhistory

import com.app.rumbo.domain.model.Publication

enum class ModerationHistoryFilter(val label: String) {
    ALL("Todas"),
    APPROVED("Aprobadas"),
    REJECTED("Rechazadas")
}

data class ModerationHistoryUiState(
    val publications: List<Publication> = emptyList(),
    val filter: ModerationHistoryFilter = ModerationHistoryFilter.ALL,
    val approvedCount: Int = 0,
    val rejectedCount: Int = 0,
    val isLoading: Boolean = true
)
