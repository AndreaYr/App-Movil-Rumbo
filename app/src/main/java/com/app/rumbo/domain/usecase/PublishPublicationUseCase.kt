package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.Author
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationDraft
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.PhotoRepository
import com.app.rumbo.domain.repository.PublicationRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Da de alta un punto de interés creado por la comunidad. Toda publicación nueva
 * nace en estado [PublicationStatus.PENDING]: un moderador debe verificarla antes
 * de que se publique oficialmente.
 */
class PublishPublicationUseCase @Inject constructor(
    private val repository: PublicationRepository,
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(
        draft: PublicationDraft,
        author: Author,
        now: Long = System.currentTimeMillis()
    ): Publication {
        val id = UUID.randomUUID().toString()
        val publication = Publication(
            id = id,
            title = draft.title,
            description = draft.description,
            category = draft.category,
            department = draft.department,
            town = draft.town,
            address = draft.address,
            schedule = draft.schedule.ifBlank { "Sin horario" },
            distanceKm = draft.distanceKm,
            price = draft.price,
            status = PublicationStatus.PENDING,
            author = author,
            publishedAt = now,
            votes = 0,
            interested = false,
            visited = false,
            latitude = draft.latitude,
            longitude = draft.longitude,
            photos = photoRepository.uploadPublicationPhotos(id, draft.photos),
            comments = emptyList()
        )
        repository.addPublication(publication)
        return publication
    }
}
