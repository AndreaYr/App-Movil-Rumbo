package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.exception.InvalidRoleException
import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.model.UserRole
import com.app.rumbo.domain.repository.UserRepository
import javax.inject.Inject

/** Autentica al usuario con Firebase Authentication y devuelve su perfil. Los moderadores solo entran por su propio acceso. */
class LoginUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * @return el usuario autenticado o null si las credenciales son inválidas.
     * @throws InvalidRoleException si la cuenta es de moderador; no se deja la sesión abierta.
     */
    suspend operator fun invoke(email: String, password: String): User? {
        val user = userRepository.login(email.trim(), password) ?: return null
        if (user.role == UserRole.MODERATOR) {
            userRepository.logout()
            throw InvalidRoleException()
        }
        return user
    }
}
