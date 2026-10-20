package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.HomeContent
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.model.Trend
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Arma los bloques de la pantalla de inicio a partir de las publicaciones:
 * - Cerca de ti: verificadas, de la más cercana a la más lejana.
 * - Más destacados: verificadas con más votos.
 * - Tendencias: verificadas cuyo interés creció más en la última semana.
 * - Nuevos lugares: las más recientes, verificadas o aún pendientes.
 */
class GetHomeUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    operator fun invoke(): Flow<HomeContent> =
        repository.observePublications().map { publications ->
            val verified = publications.filter { it.status == PublicationStatus.VERIFIED }
            HomeContent(
                nearby = verified.sortedBy { it.distanceKm }.take(NEARBY_COUNT),
                topRated = verified.sortedByDescending { it.votes }.take(TOP_COUNT),
                trending = verified
                    .filter { it.weeklyVotes > 0 }
                    .map { Trend(it, growthPercent(it)) }
                    .sortedByDescending { it.growthPercent }
                    .take(TRENDING_COUNT),
                fresh = publications
                    .filter { it.status == PublicationStatus.VERIFIED || it.status == PublicationStatus.PENDING }
                    .sortedByDescending { it.publishedAt }
                    .take(FRESH_COUNT)
            )
        }

    /** Votos de la semana frente a los que tenía antes: 64 de 100 previos = 64 %. */
    private fun growthPercent(publication: Publication): Int {
        val previousVotes = (publication.votes - publication.weeklyVotes).coerceAtLeast(1)
        return publication.weeklyVotes * 100 / previousVotes
    }

    private companion object {
        const val NEARBY_COUNT = 3
        const val TOP_COUNT = 2
        const val TRENDING_COUNT = 3
        const val FRESH_COUNT = 2
    }
}
