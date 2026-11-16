package com.app.rumbo.domain.usecase

import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.model.Author
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.ModerationActionType
import com.app.rumbo.domain.model.Photo
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.PublicationDraft
import com.app.rumbo.domain.model.User
import com.app.rumbo.testutil.FakeNotificationRepository
import com.app.rumbo.testutil.FakePhotoRepository
import com.app.rumbo.testutil.FakeUserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModerationActivityTest {

    private val repository = InMemoryPublicationRepository()
    private val notifier = ModerationNotifier(FakeNotificationRepository())
    private val approve = ApprovePublicationUseCase(repository, notifier)
    private val reject = RejectPublicationUseCase(repository, notifier)

    private val moderator = User(
        id = InMemoryPublicationRepository.MODERATOR_ID,
        name = "Laura Gómez",
        email = "laura@rumbo.co",
        city = "Armenia",
        levelName = "Moderadora"
    )

    private suspend fun publish(title: String): String {
        val draft = PublicationDraft(
            title = title,
            description = "Descripción",
            category = Category.NATURE,
            schedule = "7:00 - 18:00",
            price = PriceLevel.FREE,
            latitude = 4.58,
            longitude = -75.64,
            photos = listOf(Photo(url = "content://foto/1", description = "Foto"))
        )
        return PublishPublicationUseCase(repository, FakePhotoRepository())
            .invoke(draft, Author(id = "uid-autor", name = "Ana", levelName = "Explorador")).id
    }

    @Test
    fun `la actividad recien muestra las decisiones del moderador con su hora real`() = runTest {
        val before = System.currentTimeMillis()
        approve(publish("Mirador"))
        reject(publish("Bar"), "NOT_FOUND")

        val activity = GetRecentModerationActivityUseCase(repository, FakeUserRepository(moderator))().first()

        assertEquals(2, activity.size)
        assertEquals(setOf("Mirador", "Bar"), activity.map { it.publicationTitle }.toSet())
        assertEquals(
            ModerationActionType.REJECTED,
            activity.first { it.publicationTitle == "Bar" }.actionType
        )
        assertTrue(activity.all { it.timestamp >= before })
        assertTrue(activity.all { it.authorName == "Ana" })
    }

    @Test
    fun `la actividad no incluye lo que resolvio otro moderador`() = runTest {
        approve(publish("Mirador"))
        val otherModerator = moderator.copy(id = "otro-moderador")

        val activity = GetRecentModerationActivityUseCase(repository, FakeUserRepository(otherModerator))().first()

        assertTrue(activity.isEmpty())
    }

    @Test
    fun `sin sesion no hay actividad`() = runTest {
        approve(publish("Mirador"))

        val activity = GetRecentModerationActivityUseCase(repository, FakeUserRepository(null))().first()

        assertTrue(activity.isEmpty())
    }

    @Test
    fun `la actividad se limita a las cinco mas recientes`() = runTest {
        repeat(7) { approve(publish("Lugar $it")) }

        val activity = GetRecentModerationActivityUseCase(repository, FakeUserRepository(moderator))().first()

        assertEquals(5, activity.size)
        assertEquals(activity.sortedByDescending { it.timestamp }, activity)
    }

    @Test
    fun `el historial ordena por el momento en que se modero`() = runTest {
        val first = publish("Primera")
        val second = publish("Segunda")
        // Se resuelven en orden inverso al de publicación
        approve(second)
        Thread.sleep(5)
        approve(first)

        val history = GetModerationHistoryUseCase(repository)().first()

        assertEquals(listOf("Primera", "Segunda"), history.filter { it.title in setOf("Primera", "Segunda") }.map { it.title })
    }
}
