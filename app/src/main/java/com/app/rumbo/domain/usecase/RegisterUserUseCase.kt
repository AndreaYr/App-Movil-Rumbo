package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.model.UserRole
import com.app.rumbo.domain.repository.UserRepository
import javax.inject.Inject

/** Crea la cuenta en Firebase Authentication y guarda su perfil en Firestore. */
class RegisterUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): User {
        val user = User(
            id = "",
            name = name.trim(),
            email = email.trim(),
            city = "",
            levelName = "Explorador",
            password = password,
            role = UserRole.USER
        )
        return userRepository.createUser(user)
    }
}
