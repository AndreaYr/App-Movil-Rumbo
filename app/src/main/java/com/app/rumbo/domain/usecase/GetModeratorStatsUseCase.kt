package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.ModeratorStats
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetModeratorStatsUseCase @Inject constructor(
    private val publicationRepository: PublicationRepository
) {
    operator fun invoke(): Flow<ModeratorStats> {
        return publicationRepository.observePublications().map { publications ->
            val pending = publications.count { it.status == PublicationStatus.PENDING }
            val verified = publications.count { it.status == PublicationStatus.VERIFIED }
            val rejected = publications.count { it.status == PublicationStatus.REJECTED }
            val finished = publications.count { it.status == PublicationStatus.FINISHED }
            val total = publications.size
            ModeratorStats(
                pending = pending,
                verified = verified,
                rejected = rejected,
                finished = finished,
                total = total
            )
        }
    }
}
