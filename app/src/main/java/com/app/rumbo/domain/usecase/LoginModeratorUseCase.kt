package com.app.rumbo.domain.usecase

import androidx.annotation.StringRes
import com.app.rumbo.R
import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.model.UserRole
import com.app.rumbo.domain.repository.UserRepository
import javax.inject.Inject

class LoginModeratorException(@param:StringRes val messageRes: Int) : Exception()

/**
 * Caso de uso para iniciar sesión como moderador.
 * Autentica con Firebase Authentication y exige que la cuenta tenga rol MODERATOR.
 */
class LoginModeratorUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        val user = userRepository.login(email.trim(), password)
            ?: return Result.failure(LoginModeratorException(R.string.login_error_invalid_credentials))

        if (user.role != UserRole.MODERATOR) {
            // No se deja autenticada una cuenta normal desde la pantalla de moderador
            userRepository.logout()
            return Result.failure(LoginModeratorException(R.string.moderator_login_error_not_moderator))
        }

        return Result.success(user)
    }
}
