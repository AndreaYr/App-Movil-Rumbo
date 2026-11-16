package com.app.rumbo.domain.model

enum class ModerationActionType {
    VERIFIED,
    REJECTED
}

/** Una decisión de moderación reciente, para el panel del moderador. */
data class ModerationActivity(
    val id: String,
    val actionType: ModerationActionType,
    val publicationTitle: String,
    val authorName: String,
    /** Momento real en que se tomó la decisión; la pantalla lo muestra como "hace X". */
    val timestamp: Long
)
