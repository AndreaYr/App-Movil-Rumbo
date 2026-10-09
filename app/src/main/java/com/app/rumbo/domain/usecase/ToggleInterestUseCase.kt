package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.repository.PublicationRepository
import javax.inject.Inject

/** Marca o desmarca "Me interesa" (el voto de la comunidad). */
class ToggleInterestUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    suspend operator fun invoke(id: String) = repository.toggleInterest(id)
}
