package com.app.rumbo.data.repository

import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            levelName = "Exploradora"
        )
    )

    override fun observeCurrentUser(): Flow<User?> = currentUser.asStateFlow()
}
