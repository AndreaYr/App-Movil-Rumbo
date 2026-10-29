package com.app.rumbo.domain.usecase

import com.app.rumbo.data.local.CurrentUser
import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.Photo
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationDraft
import com.app.rumbo.domain.model.PublicationStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModerationUseCasesTest {

    private val repository = InMemoryPublicationRepository()
    private val verify = VerifyPublicationUseCase(repository)
    private val reject = RejectPublicationUseCase(repository)
    private val finish = FinishPublicationUseCase(repository)
    private val resubmit = ResubmitPublicationUseCase(repository)

    private fun draft(title: String) = PublicationDraft(
        title = title,
        description = "Descripción",
        category = Category.NATURE,
        schedule = "7:00 - 18:00",
        price = PriceLevel.CHEAP,
        latitude = 4.5871,
        longitude = -75.6429,
        photos = listOf(Photo(url = "content://foto/1", description = "Foto"))
    )

    private suspend fun publish(title: String = "Mirador"): Publication =
        PublishPublicationUseCase(repository).invoke(draft(title), CurrentUser.author)

    private suspend fun find(id: String): Publication = repository.observePublication(id).first()!!

    @Test
    fun `verificar una pendiente la deja verificada`() = runTest {
        val publication = publish()

        assertTrue(verify(publication.id))

        assertEquals(PublicationStatus.VERIFIED, find(publication.id).status)
    }

    @Test
    fun `no se puede verificar dos veces ni una publicacion inexistente`() = runTest {
        val publication = publish()
        verify(publication.id)

        assertFalse(verify(publication.id))
        assertFalse(verify("no-existe"))
    }

    @Test
    fun `rechazar guarda el motivo y el detalle`() = runTest {
        val publication = publish()

        assertTrue(reject(publication.id, " Ubicación incorrecta ", " El marcador quedó sobre la vía "))

        val rejected = find(publication.id)
        assertEquals(PublicationStatus.REJECTED, rejected.status)
        assertEquals("Ubicación incorrecta", rejected.rejectionReason)
        assertEquals("El marcador quedó sobre la vía", rejected.rejectionDetail)
    }

    @Test
    fun `rechazar sin motivo no cambia nada`() = runTest {
        val publication = publish()

        assertFalse(reject(publication.id, "  ", "detalle"))

        assertEquals(PublicationStatus.PENDING, find(publication.id).status)
    }

    @Test
    fun `solo se rechaza lo que esta pendiente`() = runTest {
        val publication = publish()
        verify(publication.id)

        assertFalse(reject(publication.id, "Ubicación incorrecta", ""))

        assertEquals(PublicationStatus.VERIFIED, find(publication.id).status)
    }

    @Test
    fun `finalizar solo funciona con publicaciones verificadas`() = runTest {
        val pending = publish("Pendiente")
        val verified = publish("Verificada")
        verify(verified.id)

        assertFalse(finish(pending.id))
        assertTrue(finish(verified.id))

        assertEquals(PublicationStatus.PENDING, find(pending.id).status)
        assertEquals(PublicationStatus.FINISHED, find(verified.id).status)
    }

    @Test
    fun `reenviar una rechazada actualiza los datos y la devuelve a pendiente`() = runTest {
        val publication = publish("Mirador")
        reject(publication.id, "Ubicación incorrecta", "Corrige el marcador")

        assertTrue(resubmit(publication.id, draft("Mirador corregido").copy(latitude = 4.6, longitude = -75.7)))

        val updated = find(publication.id)
        assertEquals(PublicationStatus.PENDING, updated.status)
        assertEquals("Mirador corregido", updated.title)
        assertEquals(4.6, updated.latitude, 0.0)
        assertNull(updated.rejectionReason)
        assertNull(updated.rejectionDetail)
    }

    @Test
    fun `solo se reenvian las publicaciones rechazadas`() = runTest {
        val publication = publish()

        assertFalse(resubmit(publication.id, draft("Otro título")))

        assertEquals("Mirador", find(publication.id).title)
    }

    @Test
    fun `las finalizables son las verificadas, la mas antigua primero`() = runTest {
        val publication = publish("Nueva")
        verify(publication.id)

        val finishable = GetFinishablePublicationsUseCase(repository).invoke().first()

        assertTrue(finishable.all { it.status == PublicationStatus.VERIFIED })
        assertEquals(finishable.sortedBy { it.publishedAt }, finishable)
        assertTrue(finishable.any { it.id == publication.id })
    }
}
