package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPublicationDetailUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    operator fun invoke(id: String): Flow<Publication?> = repository.observePublication(id)
}
