package com.app.rumbo.domain.model

/** Todo lo que necesita la pantalla "Mis estadísticas", ya calculado. */
data class UserStats(
    /** Publicaciones verificadas: las que están visibles en la comunidad. */
    val activePublications: Int,
    val finishedPublications: Int,
    val pendingPublications: Int,
    val rejectedPublications: Int,
    val commentsMade: Int,
    val votesReceived: Int,
    val points: Int,
    val visitedPlaces: Int,
    /** Categorías con publicaciones, de la que más publica a la que menos. */
    val categories: List<CategoryCount>,
    val monthlyVotes: List<MonthlyVotes>
) {
    val totalPublications: Int
        get() = activePublications + finishedPublications + pendingPublications + rejectedPublications
}

data class CategoryCount(val category: Category, val count: Int)
