package com.app.rumbo.domain.usecase

import com.app.rumbo.data.local.CurrentUser
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Publicaciones creadas por el usuario actual (marcadas con su autor). */
class GetMyPublicationsUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    operator fun invoke(): Flow<List<Publication>> =
        repository.observePublications().map { list ->
            list.filter { it.author.id == CurrentUser.author.id }
                .sortedByDescending { it.publishedAt }
        }
}
