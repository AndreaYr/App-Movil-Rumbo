package com.app.rumbo.data.repository

import com.app.rumbo.domain.model.ActivityStats
import com.app.rumbo.domain.model.MonthlyVotes
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.ActivityRepository
import com.app.rumbo.domain.repository.PublicationRepository
import com.app.rumbo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Calcula la actividad a partir de las publicaciones. Comentarios, votos, rutas y mes
 * destacado se quedan en 0 mientras esas funciones no existan; los votos por mes también,
 * porque aún no se guarda la fecha de cada voto.
 */
@Singleton
class InMemoryActivityRepository @Inject constructor(
    private val publicationRepository: PublicationRepository,
    private val userRepository: UserRepository
) : ActivityRepository {

    override fun observeActivity(): Flow<ActivityStats> =
        combine(publicationRepository.observePublications(), userRepository.observeCurrentUser()) { publications, user ->
            val mine = publications.filter { it.author.id == user?.id }
            ActivityStats(
                publications = mine.size,
                verifiedPublications = mine.count { it.status == PublicationStatus.VERIFIED },
                pendingPublications = mine.count { it.status == PublicationStatus.PENDING },
                rejectedPublications = mine.count { it.status == PublicationStatus.REJECTED },
                finishedPublications = mine.count { it.status == PublicationStatus.FINISHED },
                publicationsByCategory = mine.groupingBy { it.category }.eachCount(),
                monthlyVotes = MonthlyVotes.emptyWindow(System.currentTimeMillis()),
                visitedPlaces = publications.count { it.visited }
            )
        }
}
