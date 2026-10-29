package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Cierra una publicación verificada (por ejemplo un evento ya terminado). Deja de aparecer
 * en el feed activo y en el planificador de rutas, y no se puede volver a abrir.
 */
class FinishPublicationUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    /** @return `false` si la publicación no existe o no está verificada. */
    suspend operator fun invoke(id: String): Boolean {
        val publication = repository.observePublication(id).first() ?: return false
        if (publication.status != PublicationStatus.VERIFIED) return false
        repository.updateStatus(id, PublicationStatus.FINISHED)
        return true
    }
}
