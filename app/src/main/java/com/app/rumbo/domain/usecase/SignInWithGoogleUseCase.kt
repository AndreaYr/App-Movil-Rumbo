package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.exception.InvalidRoleException
import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.model.UserRole
import com.app.rumbo.domain.repository.UserRepository
import javax.inject.Inject

/** Inicia sesión o registra al usuario con su cuenta de Google. Los moderadores solo entran por su propio acceso. */
class SignInWithGoogleUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /** @throws InvalidRoleException si la cuenta de Google pertenece a un moderador; no se deja la sesión abierta. */
    suspend operator fun invoke(idToken: String): User {
        val user = userRepository.signInWithGoogle(idToken)
        if (user.role == UserRole.MODERATOR) {
            userRepository.logout()
            throw InvalidRoleException()
        }
        return user
    }
}
