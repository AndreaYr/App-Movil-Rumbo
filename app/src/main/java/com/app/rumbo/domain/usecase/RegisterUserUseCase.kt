package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.repository.UserRepository
import java.util.UUID
import javax.inject.Inject

/** Crea un usuario en memoria y lo establece como usuario actual. */
class RegisterUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String, email: String): User {
        val user = User(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            email = email.trim(),
            city = "",
            levelName = "Explorador"
        )
        return userRepository.createUser(user)
    }
}
