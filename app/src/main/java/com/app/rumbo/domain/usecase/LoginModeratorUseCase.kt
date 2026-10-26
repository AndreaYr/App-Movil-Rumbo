package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.repository.UserRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Caso de uso para iniciar sesión como moderador.
 * Valida credenciales institucionales y retorna el usuario si son correctas.
 */
class LoginModeratorUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        delay(SIMULATED_REQUEST_DELAY_MS)
        val normalizedEmail = email.trim()
        val isValid = normalizedEmail.equals(MODERATOR_EMAIL, ignoreCase = true) &&
                password == MODERATOR_PASSWORD

        return if (isValid) {
            val moderatorUser = User(
                id = "moderator",
                name = "Moderador",
                email = normalizedEmail,
                city = "Armenia, Quindío",
                levelName = "Moderador",
                bio = "Cuenta de moderación"
            )
            Result.success(moderatorUser)
        } else {
            Result.failure(Exception("Correo o contraseña incorrectos."))
        }
    }

    private companion object {
        const val MODERATOR_EMAIL = "moderador@rumbo.app"
        const val MODERATOR_PASSWORD = "mod12345"
        const val SIMULATED_REQUEST_DELAY_MS = 800L
    }
}
