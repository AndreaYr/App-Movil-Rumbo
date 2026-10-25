package com.app.rumbo.domain.model

enum class ModerationActionType {
    VERIFIED,
    REJECTED
}

data class ModerationActivity(
    val id: String,
    val actionType: ModerationActionType,
    val publicationTitle: String,
    val authorName: String,
    val timeAgo: String
)
