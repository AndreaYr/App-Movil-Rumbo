package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.Author
import com.app.rumbo.domain.repository.AuthorRepository
import com.app.rumbo.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/** Autor del usuario que tiene la sesión iniciada; null si no hay ninguno. */
class GetCurrentAuthorUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val authorRepository: AuthorRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Author?> =
        userRepository.observeCurrentUser().flatMapLatest { user ->
            if (user == null) flowOf(null) else authorRepository.observeAuthor(user.id)
        }
}
