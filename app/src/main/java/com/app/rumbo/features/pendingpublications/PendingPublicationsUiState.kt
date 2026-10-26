package com.app.rumbo.features.pendingpublications

import com.app.rumbo.domain.model.Publication

data class PendingPublicationsUiState(
    val publications: List<Publication> = emptyList(),
    val isLoading: Boolean = false,
    val pendingCount: Int = 0
)
