package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Usuario que inició sesión; null si no hay ninguno. */
class GetCurrentUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<User?> = repository.observeCurrentUser()
}
