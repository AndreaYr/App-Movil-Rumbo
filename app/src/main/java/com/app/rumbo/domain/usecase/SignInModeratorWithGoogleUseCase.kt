package com.app.rumbo.domain.usecase

import com.app.rumbo.R
import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.model.UserRole
import com.app.rumbo.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Inicia sesión como moderador con Google. La cuenta debe estar registrada y tener rol MODERATOR:
 * esta pantalla nunca crea cuentas ni deja entrar a usuarios normales.
 */
class SignInModeratorWithGoogleUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(idToken: String): Result<User> {
        val user = userRepository.signInExistingWithGoogle(idToken)
            ?: return Result.failure(LoginModeratorException(R.string.auth_error_invalid_credentials_or_role))
        if (user.role != UserRole.MODERATOR) {
            userRepository.logout()
            return Result.failure(LoginModeratorException(R.string.auth_error_invalid_credentials_or_role))
        }
        return Result.success(user)
    }
}
