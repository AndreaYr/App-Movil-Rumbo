package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.repository.PublicationRepository
import javax.inject.Inject

class RejectPublicationUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    suspend operator fun invoke(publicationId: String, reason: String): Boolean =
        repository.rejectPublication(publicationId, reason)
}
