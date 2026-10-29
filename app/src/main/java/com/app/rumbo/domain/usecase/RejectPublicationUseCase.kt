package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.repository.PublicationRepository
import javax.inject.Inject

/** El moderador rechaza una publicación pendiente; el motivo es obligatorio y lo ve el autor. */
class RejectPublicationUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    /** @return `false` si falta el motivo, la publicación no existe o ya no está pendiente. */
    suspend operator fun invoke(publicationId: String, reason: String, detail: String = ""): Boolean =
        repository.rejectPublication(publicationId, reason, detail)
}
