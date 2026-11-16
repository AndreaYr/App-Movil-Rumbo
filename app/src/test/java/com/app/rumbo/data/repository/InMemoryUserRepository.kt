package com.app.rumbo.data.repository

import com.app.rumbo.domain.model.ProfileUpdate
import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Usuario en memoria para pruebas. No define cuentas por defecto: todas se crean
 * mediante registro o login, igual que en el repositorio real.
 */
@Singleton
class InMemoryUserRepository @Inject constructor() : UserRepository {

    private val users = mutableMapOf<String, User>()

    private val currentUser = MutableStateFlow<User?>(null)

    override fun observeCurrentUser(): Flow<User?> = currentUser.asStateFlow()

    override suspend fun createUser(user: User): User {
        val saved = user.copy(id = user.id.ifBlank { UUID.randomUUID().toString() })
        users[saved.email] = saved
        currentUser.value = saved
        return saved
    }

    override suspend fun login(email: String, password: String): User? {
        val user = users[email] ?: return null
        if (user.password != password) return null
        currentUser.value = user
        return user
    }

    override suspend fun signInWithGoogle(idToken: String): User {
        // Igual que el repositorio real: si la cuenta ya tiene perfil se usa ese, si no se crea uno de usuario
        val email = "$idToken@google.test"
        val user = users[email] ?: User(id = idToken, name = "Google", email = email, city = "", levelName = "Explorador")
        users[user.email] = user
        currentUser.value = user
        return user
    }

    override suspend fun signInExistingWithGoogle(idToken: String): User? {
        val user = users["$idToken@google.test"] ?: return null
        currentUser.value = user
        return user
    }

    override suspend fun sendPasswordReset(email: String) = Unit

    override suspend fun logout() {
        currentUser.value = null
    }

    override suspend fun updateProfile(update: ProfileUpdate) {
        currentUser.update { user ->
            user?.copy(name = update.name, bio = update.bio, city = update.city)
                ?.also { users[it.email] = it }
        }
    }
}
