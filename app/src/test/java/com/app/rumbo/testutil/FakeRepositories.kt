package com.app.rumbo.testutil

import com.app.rumbo.domain.model.ActivityStats
import com.app.rumbo.domain.model.ProfileUpdate
import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.repository.ActivityRepository
import com.app.rumbo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Actividad controlada por la prueba: se cambia `stats` para simular que el usuario hace cosas. */
class FakeActivityRepository(initial: ActivityStats = ActivityStats()) : ActivityRepository {
    val stats = MutableStateFlow(initial)
    override fun observeActivity(): Flow<ActivityStats> = stats.asStateFlow()
}

/** Usuario controlado por la prueba; con `null` simula que no hay sesión iniciada. */
class FakeUserRepository(user: User? = TestUsers.mariana) : UserRepository {
    private val current = MutableStateFlow(user)
    override fun observeCurrentUser(): Flow<User?> = current.asStateFlow()
    override suspend fun createUser(user: User): User {
        current.value = user
        return user
    }
    override suspend fun updateProfile(update: ProfileUpdate) {
        current.update { it?.copy(name = update.name, bio = update.bio, city = update.city) }
    }
}

object TestUsers {
    val mariana = User(
        id = "me",
        name = "Mariana Ospina",
        email = "mariana.ospina@correo.co",
        city = "Armenia, Quindío",
        levelName = "Exploradora",
        bio = "Caminante de fin de semana."
    )
}
