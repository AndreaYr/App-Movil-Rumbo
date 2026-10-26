package com.app.rumbo.features.moderationfinished

data class ModerationFinishedUiState(
    val approved: Boolean = true,
    val rejectionReason: String? = null
)
