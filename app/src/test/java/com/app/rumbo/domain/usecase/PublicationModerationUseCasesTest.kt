package com.app.rumbo.domain.usecase

import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.model.FeedSort
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PublicationModerationUseCasesTest {

    @Test
    fun `aprobar actualiza estado compartido, quita de pendientes y publica en Feed y Home`() = runTest {
        val repository = InMemoryPublicationRepository()
        val pending = GetPendingPublicationsUseCase(repository)
        val feed = GetFeedUseCase(repository)
        val home = GetHomeUseCase(repository)
        val publicationId = "moderation-test"
        val pendingSample = repository.observePublication("murales-filandia").first()!!
        repository.addPublication(
            pendingSample.copy(id = publicationId, publishedAt = System.currentTimeMillis() + 1_000)
        )

        assertTrue(pending().first().any { it.id == publicationId })
        assertFalse(feed(null, FeedSort.RECENT).first().any { it.id == publicationId })

        assertTrue(ApprovePublicationUseCase(repository)(publicationId))

        assertTrue(pending().first().none { it.id == publicationId })
        assertEquals(PublicationStatus.VERIFIED, repository.observePublication(publicationId).first()?.status)
        assertTrue(feed(null, FeedSort.RECENT).first().any { it.id == publicationId })
        assertTrue(home().first().fresh.any { it.id == publicationId })
    }

    @Test
    fun `rechazar conserva el motivo, quita de pendientes y excluye de Feed y Home`() = runTest {
        val repository = InMemoryPublicationRepository()
        val publicationId = "murales-filandia"
        val reason = "Ubicación incorrecta: el marcador está sobre la vía principal."

        assertTrue(RejectPublicationUseCase(repository)(publicationId, reason))

        val rejected = repository.observePublication(publicationId).first()
        assertEquals(PublicationStatus.REJECTED, rejected?.status)
        assertEquals(reason, rejected?.rejectionReason)
        assertTrue(GetPendingPublicationsUseCase(repository)().first().none { it.id == publicationId })
        assertFalse(
            GetFeedUseCase(repository)(null, FeedSort.RECENT)
                .first().any { it.id == publicationId }
        )
        assertFalse(GetHomeUseCase(repository)().first().fresh.any { it.id == publicationId })
    }

    @Test
    fun `solo se pueden moderar publicaciones pendientes y el rechazo exige motivo`() = runTest {
        val repository = InMemoryPublicationRepository()

        assertFalse(ApprovePublicationUseCase(repository)("valle-cocora"))
        assertFalse(RejectPublicationUseCase(repository)("murales-filandia", " "))
        assertNull(repository.observePublication("murales-filandia").first()?.rejectionReason)
    }
}
