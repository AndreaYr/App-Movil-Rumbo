package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.repository.UserRepository
import javax.inject.Inject

/** Envía el enlace de recuperación de contraseña al correo indicado. */
class SendPasswordResetUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String) = userRepository.sendPasswordReset(email.trim())
}
