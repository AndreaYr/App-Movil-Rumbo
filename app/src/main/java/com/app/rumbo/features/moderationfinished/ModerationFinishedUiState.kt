package com.app.rumbo.features.moderationfinished

data class ModerationFinishedUiState(
    val outcome: ModerationOutcome = ModerationOutcome.APPROVED
) {
    val title: String get() = outcome.title
    val message: String get() = outcome.message
}
