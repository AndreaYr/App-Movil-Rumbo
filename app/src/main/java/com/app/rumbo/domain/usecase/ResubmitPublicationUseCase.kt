package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.PublicationDraft
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** El autor corrige una publicación rechazada y la reenvía: vuelve a la cola del moderador. */
class ResubmitPublicationUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    /** @return `false` si la publicación no existe o no está rechazada. */
    suspend operator fun invoke(id: String, draft: PublicationDraft): Boolean {
        val publication = repository.observePublication(id).first() ?: return false
        if (publication.status != PublicationStatus.REJECTED) return false
        repository.updatePublication(
            publication.copy(
                title = draft.title,
                description = draft.description,
                category = draft.category,
                schedule = draft.schedule.ifBlank { "Sin horario" },
                price = draft.price,
                latitude = draft.latitude,
                longitude = draft.longitude,
                status = PublicationStatus.PENDING,
                rejectionReason = null,
                rejectionDetail = null
            )
        )
        return true
    }
}
