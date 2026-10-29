package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.ActivityStats
import com.app.rumbo.domain.model.CategoryCount
import com.app.rumbo.domain.model.UserStats
import com.app.rumbo.domain.model.earnedPoints
import com.app.rumbo.domain.model.toBadges
import com.app.rumbo.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Convierte la actividad del usuario en las cifras de "Mis estadísticas". Los puntos salen
 * de los mismos logros que usa el Perfil, así que ambas pantallas siempre coinciden.
 */
class GetStatsUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    operator fun invoke(): Flow<UserStats> = repository.observeActivity().map { it.toStats() }

    private fun ActivityStats.toStats() = UserStats(
        activePublications = verifiedPublications,
        finishedPublications = finishedPublications,
        pendingPublications = pendingPublications,
        rejectedPublications = rejectedPublications,
        commentsMade = commentsMade,
        votesReceived = votesReceived,
        points = toBadges().earnedPoints(),
        visitedPlaces = visitedPlaces,
        categories = publicationsByCategory
            .map { (category, count) -> CategoryCount(category, count) }
            .sortedByDescending { it.count },
        monthlyVotes = monthlyVotes
    )
}
