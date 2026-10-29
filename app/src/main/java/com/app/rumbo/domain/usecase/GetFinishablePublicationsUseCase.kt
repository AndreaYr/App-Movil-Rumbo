package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Publicaciones verificadas que el moderador puede cerrar, las más antiguas primero. */
class GetFinishablePublicationsUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    operator fun invoke(): Flow<List<Publication>> =
        repository.observePublications().map { list ->
            list.filter { it.status == PublicationStatus.VERIFIED }.sortedBy { it.publishedAt }
        }
}
