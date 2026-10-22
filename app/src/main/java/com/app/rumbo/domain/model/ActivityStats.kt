package com.app.rumbo.domain.model

/** Actividad acumulada del usuario: la materia prima con la que se miden los logros. */
data class ActivityStats(
    val publications: Int = 0,
    val verifiedPublications: Int = 0,
    val pendingPublications: Int = 0,
    val visitedPlaces: Int = 0,
    val commentsMade: Int = 0,
    val votesReceived: Int = 0,
    val routesCreated: Int = 0,
    val routesCompleted: Int = 0,
    /** Veces que una publicación suya fue la más votada del mes. */
    val monthlyTopPublications: Int = 0
)
