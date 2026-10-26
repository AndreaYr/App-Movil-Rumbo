package com.app.rumbo.domain.model

/** Actividad acumulada del usuario: la materia prima con la que se miden los logros. */
data class ActivityStats(
    val publications: Int = 0,
    val verifiedPublications: Int = 0,
    val pendingPublications: Int = 0,
    val rejectedPublications: Int = 0,
    val finishedPublications: Int = 0,
    /** Publicaciones del usuario por categoría; las categorías sin publicaciones no aparecen. */
    val publicationsByCategory: Map<Category, Int> = emptyMap(),
    /** Votos recibidos en los últimos meses, del más antiguo al más reciente. */
    val monthlyVotes: List<MonthlyVotes> = emptyList(),
    val visitedPlaces: Int = 0,
    val commentsMade: Int = 0,
    val votesReceived: Int = 0,
    val routesCreated: Int = 0,
    val routesCompleted: Int = 0,
    /** Veces que una publicación suya fue la más votada del mes. */
    val monthlyTopPublications: Int = 0
)
