package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.repository.UserRepository
import javax.inject.Inject

/** Autentica al usuario con Firebase Authentication y devuelve su perfil. */
class LoginUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /** @return el usuario autenticado o null si las credenciales son inválidas. */
    suspend operator fun invoke(email: String, password: String): User? =
        userRepository.login(email.trim(), password)
}
