package com.app.rumbo.domain.repository

import com.app.rumbo.domain.model.ProfileUpdate
import com.app.rumbo.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /** Usuario actual compartido por la aplicación; null si no hay ninguno. */
    fun observeCurrentUser(): Flow<User?>

    /** Guarda al usuario recién creado y lo deja seleccionado como usuario actual. */
    suspend fun createUser(user: User): User

    /** Guarda los cambios del perfil del usuario actual. */
    suspend fun updateProfile(update: ProfileUpdate)
}
