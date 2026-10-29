package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** El moderador rechaza una publicación pendiente; el motivo es obligatorio y lo ve el autor. */
class RejectPublicationUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    /** @return `false` si falta el motivo, la publicación no existe o ya no está pendiente. */
    suspend operator fun invoke(id: String, reason: String, detail: String): Boolean {
        if (reason.isBlank()) return false
        val publication = repository.observePublication(id).first() ?: return false
        if (publication.status != PublicationStatus.PENDING) return false
        repository.updateStatus(
            id = id,
            status = PublicationStatus.REJECTED,
            rejectionReason = reason.trim(),
            rejectionDetail = detail.trim()
        )
        return true
    }
}
