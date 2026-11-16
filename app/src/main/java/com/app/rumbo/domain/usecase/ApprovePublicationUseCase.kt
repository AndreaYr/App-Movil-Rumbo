package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.NotificationType
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** El moderador verifica una publicación pendiente y se lo avisa a su autor. */
class ApprovePublicationUseCase @Inject constructor(
    private val repository: PublicationRepository,
    private val notifier: ModerationNotifier
) {
    /** @return `false` si la publicación no existe o ya no está pendiente (por ejemplo, otro moderador la resolvió). */
    suspend operator fun invoke(publicationId: String): Boolean {
        val publication = repository.observePublication(publicationId).first()
        val approved = repository.approvePublication(publicationId)
        if (approved) notifier.notifyAuthor(publication, NotificationType.PUBLICATION_VERIFIED)
        return approved
    }
}
