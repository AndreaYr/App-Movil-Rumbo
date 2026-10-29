package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** El moderador aprueba una publicación pendiente: queda visible para toda la comunidad. */
class VerifyPublicationUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    /** @return `false` si la publicación no existe o ya no está pendiente. */
    suspend operator fun invoke(id: String): Boolean {
        val publication = repository.observePublication(id).first() ?: return false
        if (publication.status != PublicationStatus.PENDING) return false
        repository.updateStatus(id, PublicationStatus.VERIFIED)
        return true
    }
}
