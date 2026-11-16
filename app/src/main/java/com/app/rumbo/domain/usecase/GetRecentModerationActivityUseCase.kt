package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.ModerationActionType
import com.app.rumbo.domain.model.ModerationActivity
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.PublicationRepository
import com.app.rumbo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Las últimas decisiones que tomó el moderador con sesión iniciada, de la más reciente a la más antigua. */
class GetRecentModerationActivityUseCase @Inject constructor(
    private val publicationRepository: PublicationRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(limit: Int = DEFAULT_LIMIT): Flow<List<ModerationActivity>> =
        combine(publicationRepository.observePublications(), userRepository.observeCurrentUser()) { publications, user ->
            if (user == null) return@combine emptyList()
            publications
                .filter {
                    (it.status == PublicationStatus.VERIFIED || it.status == PublicationStatus.REJECTED) &&
                        it.moderatedBy == user.id
                }
                .sortedByDescending { it.moderationTime }
                .take(limit)
                .map { publication ->
                    ModerationActivity(
                        id = publication.id,
                        actionType = if (publication.status == PublicationStatus.VERIFIED) {
                            ModerationActionType.VERIFIED
                        } else {
                            ModerationActionType.REJECTED
                        },
                        publicationTitle = publication.title,
                        authorName = publication.author.name,
                        timestamp = publication.moderationTime
                    )
                }
        }

    private companion object {
        const val DEFAULT_LIMIT = 5
    }
}
