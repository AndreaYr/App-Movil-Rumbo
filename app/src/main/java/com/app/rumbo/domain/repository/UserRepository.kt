package com.app.rumbo.domain.repository

import com.app.rumbo.domain.model.ProfileUpdate
import com.app.rumbo.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /** Usuario que inició sesión; null si no hay ninguno. */
    fun observeCurrentUser(): Flow<User?>

    /** Guarda los cambios del perfil del usuario actual. */
    suspend fun updateProfile(update: ProfileUpdate)
}
