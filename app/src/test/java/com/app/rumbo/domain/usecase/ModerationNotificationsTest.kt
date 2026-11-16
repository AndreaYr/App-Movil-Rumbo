package com.app.rumbo.domain.usecase

import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.model.Author
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.Photo
import com.app.rumbo.domain.model.NotificationType
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationDraft
import com.app.rumbo.testutil.FakeNotificationRepository
import com.app.rumbo.testutil.FakePhotoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModerationNotificationsTest {

    private val repository = InMemoryPublicationRepository()
    private val notifications = FakeNotificationRepository()
    private val notifier = ModerationNotifier(notifications)
    private val approve = ApprovePublicationUseCase(repository, notifier)
    private val reject = RejectPublicationUseCase(repository, notifier)

    private suspend fun publish(author: Author = Author(id = "uid-autor", name = "Ana", levelName = "Explorador")): Publication {
        val draft = PublicationDraft(
            title = "Mirador",
            description = "Descripción",
            category = Category.NATURE,
            schedule = "7:00 - 18:00",
            price = PriceLevel.CHEAP,
            latitude = 4.58,
            longitude = -75.64,
            photos = listOf(Photo(url = "content://foto/1", description = "Foto"))
        )
        return PublishPublicationUseCase(repository, FakePhotoRepository()).invoke(draft, author)
    }

    @Test
    fun `al verificar el autor recibe un aviso con el titulo`() = runTest {
        val publication = publish()

        assertTrue(approve(publication.id))

        val (userId, notification) = notifications.sent.single()
        assertEquals(publication.author.id, userId)
        assertEquals(NotificationType.PUBLICATION_VERIFIED, notification.type)
        assertEquals("Mirador", notification.publicationTitle)
        assertEquals(publication.id, notification.publicationId)
        assertFalse(notification.isRead)
    }

    @Test
    fun `al rechazar el autor recibe un aviso de rechazo`() = runTest {
        val publication = publish()

        assertTrue(reject(publication.id, "Ubicación incorrecta"))

        assertEquals(NotificationType.PUBLICATION_REJECTED, notifications.sent.single().second.type)
    }

    @Test
    fun `si otro moderador ya resolvio la publicacion no se avisa dos veces`() = runTest {
        val publication = publish()
        approve(publication.id)

        assertFalse(approve(publication.id))
        assertFalse(reject(publication.id, "Fotos insuficientes"))

        assertEquals(1, notifications.sent.size)
    }

    @Test
    fun `si el aviso falla la moderacion igual se completa`() = runTest {
        notifications.failNotify = true
        val publication = publish()

        assertTrue(approve(publication.id))
        assertTrue(notifications.sent.isEmpty())
    }

    @Test
    fun `un motivo vacio no rechaza ni avisa`() = runTest {
        val publication = publish()

        assertFalse(reject(publication.id, "  "))

        assertTrue(notifications.sent.isEmpty())
    }

    @Test
    fun `no se avisa si el autor no es un usuario real`() = runTest {
        val publication = publish(Author(id = "me", name = "Tú", levelName = "Explorador"))

        approve(publication.id)

        assertTrue(notifications.sent.isEmpty())
    }
}
