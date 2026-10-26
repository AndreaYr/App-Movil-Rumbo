package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.ModerationActionType
import com.app.rumbo.domain.model.ModerationActivity
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetRecentModerationActivityUseCase @Inject constructor(
    private val publicationRepository: PublicationRepository
) {
    operator fun invoke(): Flow<List<ModerationActivity>> {
        return publicationRepository.observePublications().map { publications ->
            publications
                .filter { it.status == PublicationStatus.VERIFIED || it.status == PublicationStatus.REJECTED }
                .sortedByDescending { it.publishedAt }
                .take(5)
                .mapIndexed { index, publication ->
                    ModerationActivity(
                        id = publication.id + "_activity_$index",
                        actionType = if (publication.status == PublicationStatus.VERIFIED) {
                            ModerationActionType.VERIFIED
                        } else {
                            ModerationActionType.REJECTED
                        },
                        publicationTitle = if (publication.status == PublicationStatus.VERIFIED) {
                            "Verificaste ${publication.title}"
                        } else {
                            "Rechazaste ${publication.title}"
                        },
                        authorName = publication.author.name,
                        timeAgo = when (index) {
                            0 -> "hace 25 min"
                            1 -> "hace 1 h"
                            2 -> "hace 2 h"
                            3 -> "hace ayer"
                            else -> "hace 2 días"
                        }
                    )
                }
        }
    }
}
