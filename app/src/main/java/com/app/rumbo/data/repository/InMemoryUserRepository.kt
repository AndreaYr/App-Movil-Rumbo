package com.app.rumbo.data.repository

import com.app.rumbo.domain.model.ProfileUpdate
import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Usuario en memoria. Mientras el login real no exista, arranca con un usuario de
 * ejemplo; cuando se implemente la autenticación, el inicio de sesión lo reemplazará.
 */
@Singleton
class InMemoryUserRepository @Inject constructor() : UserRepository {

    private val currentUser = MutableStateFlow<User?>(
        User(
            id = "me",
            name = "Mariana Ospina",
            email = "mariana.ospina@correo.co",
            city = "Armenia, Quindío",
            levelName = "Exploradora",
            bio = "Caminante de fin de semana. Busco miradores y cafés de finca en el Quindío."
        )
    )

    override fun observeCurrentUser(): Flow<User?> = currentUser.asStateFlow()

    override suspend fun createUser(user: User): User {
        currentUser.value = user
        return user
    }

    override suspend fun updateProfile(update: ProfileUpdate) {
        currentUser.update { user ->
            user?.copy(name = update.name, bio = update.bio, city = update.city)
        }
    }
}
