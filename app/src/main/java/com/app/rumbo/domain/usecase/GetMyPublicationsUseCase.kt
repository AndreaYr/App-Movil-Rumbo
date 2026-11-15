package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.repository.PublicationRepository
import com.app.rumbo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Publicaciones creadas por el usuario actual (marcadas con su autor). */
class GetMyPublicationsUseCase @Inject constructor(
    private val repository: PublicationRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<List<Publication>> =
        combine(repository.observePublications(), userRepository.observeCurrentUser()) { publications, user ->
            val userId = user?.id ?: return@combine emptyList()
            publications.filter { it.author.id == userId }
                .sortedByDescending { it.publishedAt }
        }
}
