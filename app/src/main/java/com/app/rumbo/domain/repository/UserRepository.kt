package com.app.rumbo.domain.repository

import com.app.rumbo.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /** Usuario que inició sesión; null si no hay ninguno. */
    fun observeCurrentUser(): Flow<User?>
}
