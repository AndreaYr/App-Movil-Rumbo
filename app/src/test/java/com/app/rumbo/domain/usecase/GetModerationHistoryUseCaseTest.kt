package com.app.rumbo.domain.usecase

import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.model.PublicationStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetModerationHistoryUseCaseTest {

    @Test
    fun `incluye solo publicaciones aprobadas y rechazadas ordenadas por fecha`() = runTest {
        val repository = InMemoryPublicationRepository()
        val base = repository.observePublication("murales-filandia").first()!!
        val now = System.currentTimeMillis()

        repository.addPublication(
            base.copy(
                id = "approved-history",
                title = "Aprobada",
                status = PublicationStatus.VERIFIED,
                publishedAt = now + 1_000
            )
        )
        repository.addPublication(
            base.copy(
                id = "rejected-history",
                title = "Rechazada",
                status = PublicationStatus.REJECTED,
                publishedAt = now + 2_000,
                rejectionReason = "Fotografías no válidas"
            )
        )
        repository.addPublication(
            base.copy(
                id = "pending-history",
                title = "Pendiente",
                status = PublicationStatus.PENDING,
                publishedAt = now + 3_000
            )
        )

        val history = GetModerationHistoryUseCase(repository)().first()

        assertTrue(history.none { it.id == "pending-history" })
        assertTrue(history.all { it.status == PublicationStatus.VERIFIED || it.status == PublicationStatus.REJECTED })
        assertEquals("rejected-history", history.first().id)
        assertEquals("approved-history", history[1].id)
        assertEquals("Fotografías no válidas", history.first().rejectionReason)
    }
}
