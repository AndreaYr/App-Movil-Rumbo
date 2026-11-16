package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.repository.UserRepository
import javax.inject.Inject

/** Inicia sesión o registra al usuario con su cuenta de Google. */
class SignInWithGoogleUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(idToken: String): User = userRepository.signInWithGoogle(idToken)
}
