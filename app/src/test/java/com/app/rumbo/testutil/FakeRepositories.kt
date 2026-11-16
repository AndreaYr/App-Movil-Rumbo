package com.app.rumbo.testutil

import com.app.rumbo.domain.model.ActivityStats
import com.app.rumbo.domain.model.Author
import com.app.rumbo.domain.model.Notification
import com.app.rumbo.domain.model.Photo
import com.app.rumbo.domain.model.ProfileUpdate
import com.app.rumbo.domain.model.SavedRoute
import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.repository.ActivityRepository
import com.app.rumbo.domain.repository.AuthorRepository
import com.app.rumbo.domain.repository.NotificationRepository
import com.app.rumbo.domain.repository.PhotoRepository
import com.app.rumbo.domain.repository.SavedRouteRepository
import com.app.rumbo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update

/** Actividad controlada por la prueba: se cambia `stats` para simular que el usuario hace cosas. */
class FakeActivityRepository(initial: ActivityStats = ActivityStats()) : ActivityRepository {
    val stats = MutableStateFlow(initial)
    override fun observeActivity(): Flow<ActivityStats> = stats.asStateFlow()
}

/** Autor asociado a un usuario de prueba. */
class FakeAuthorRepository(private val user: User? = TestUsers.mariana) : AuthorRepository {
    override fun observeAuthor(id: String): Flow<Author?> =
        flowOf(user?.takeIf { it.id == id }?.toAuthor())
}

/** Fotos que se "suben" sin salir del dispositivo. */
class FakePhotoRepository : PhotoRepository {
    override suspend fun uploadPublicationPhotos(
        publicationId: String,
        photos: List<Photo>
    ): List<Photo> = photos
}

/** Avisos en memoria controlados por la prueba. */
class FakeNotificationRepository : NotificationRepository {
    val notifications = MutableStateFlow<List<Notification>>(emptyList())
    override fun observeNotifications(): Flow<List<Notification>> = notifications.asStateFlow()
    override suspend fun markAsRead(id: String) {
        notifications.update { list -> list.map { if (it.id == id) it.copy(isRead = true) else it } }
    }
    override suspend fun markAllAsRead() {
        notifications.update { list -> list.map { it.copy(isRead = true) } }
    }
    /** Avisos creados con [notifyUser], por id de usuario destinatario. */
    val sent = mutableListOf<Pair<String, Notification>>()
    var failNotify = false
    override suspend fun notifyUser(userId: String, notification: Notification) {
        if (failNotify) throw java.io.IOException("Sin permisos")
        sent += userId to notification
    }
}

/** Rutas guardadas en memoria; con `failWrites` simula un error de almacenamiento. */
class FakeSavedRouteRepository(var failWrites: Boolean = false) : SavedRouteRepository {
    val routes = MutableStateFlow<List<SavedRoute>>(emptyList())
    override fun observeRoutes(): Flow<List<SavedRoute>> = routes.asStateFlow()
    override suspend fun saveRoute(route: SavedRoute) {
        if (failWrites) throw java.io.IOException("Sin espacio")
        routes.update { listOf(route) + it }
    }
}

/** Usuario controlado por la prueba; con `null` simula que no hay sesión iniciada. */
class FakeUserRepository(user: User? = TestUsers.mariana) : UserRepository {
    private val current = MutableStateFlow(user)
    override fun observeCurrentUser(): Flow<User?> = current.asStateFlow()
    override suspend fun createUser(user: User): User {
        current.value = user
        return user
    }
    override suspend fun login(email: String, password: String): User? =
        current.value?.takeIf { it.email == email }?.also { current.value = it }
    override suspend fun signInWithGoogle(idToken: String): User =
        (current.value ?: TestUsers.mariana).also { current.value = it }
    override suspend fun signInExistingWithGoogle(idToken: String): User? = current.value
    override suspend fun sendPasswordReset(email: String) = Unit
    override suspend fun logout() {
        current.value = null
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

fun User.toAuthor() = Author(id = id, name = name, levelName = levelName)
