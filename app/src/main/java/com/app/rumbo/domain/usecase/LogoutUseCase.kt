package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.repository.UserRepository
import javax.inject.Inject

/** Cierra la sesión actual en Firebase Authentication. */
class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke() = userRepository.logout()
}
