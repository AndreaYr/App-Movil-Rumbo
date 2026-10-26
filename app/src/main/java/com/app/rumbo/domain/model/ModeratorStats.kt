package com.app.rumbo.domain.model

data class ModeratorStats(
    val pending: Int = 0,
    val verified: Int = 0,
    val rejected: Int = 0,
    val finished: Int = 0,
    val total: Int = 0
)
