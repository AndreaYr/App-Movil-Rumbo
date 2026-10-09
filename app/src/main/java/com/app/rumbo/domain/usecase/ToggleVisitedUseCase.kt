package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.repository.PublicationRepository
import javax.inject.Inject

class ToggleVisitedUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    suspend operator fun invoke(id: String) = repository.toggleVisited(id)
}
