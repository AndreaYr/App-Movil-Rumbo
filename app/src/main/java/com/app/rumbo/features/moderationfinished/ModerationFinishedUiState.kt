package com.app.rumbo.features.moderationfinished

data class ModerationFinishedUiState(
    val outcome: ModerationOutcome = ModerationOutcome.APPROVED
)
