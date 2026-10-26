package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPendingPublicationsUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    operator fun invoke(): Flow<List<Publication>> =
        repository.observePublications().map { publications ->
            publications.filter { it.status == PublicationStatus.PENDING }
                .sortedBy { it.publishedAt }
        }
}
