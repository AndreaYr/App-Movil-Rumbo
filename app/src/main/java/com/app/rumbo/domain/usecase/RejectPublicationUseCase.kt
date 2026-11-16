package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.NotificationType
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** El moderador rechaza una publicación pendiente; el motivo es obligatorio y lo ve el autor, que además recibe un aviso. */
class RejectPublicationUseCase @Inject constructor(
    private val repository: PublicationRepository,
    private val notifier: ModerationNotifier
) {
    /** @return `false` si falta el motivo, la publicación no existe o ya no está pendiente. */
    suspend operator fun invoke(publicationId: String, reason: String, detail: String = ""): Boolean {
        if (reason.isBlank()) return false
        val publication = repository.observePublication(publicationId).first()
        val rejected = repository.rejectPublication(publicationId, reason, detail)
        if (rejected) notifier.notifyAuthor(publication, NotificationType.PUBLICATION_REJECTED)
        return rejected
    }
}
