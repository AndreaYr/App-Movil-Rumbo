package com.app.rumbo.data.repository

import com.app.rumbo.data.local.SamplePublications
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** Repositorio en memoria: los cambios duran mientras la app esté abierta. */
@Singleton
class InMemoryPublicationRepository @Inject constructor() : PublicationRepository {

    private val publications = MutableStateFlow(SamplePublications.create())

    override fun observePublications(): Flow<List<Publication>> = publications.asStateFlow()

    override fun observePublication(id: String): Flow<Publication?> =
        publications.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun addPublication(publication: Publication) {
        publications.update { list -> listOf(publication) + list }
    }

    override suspend fun approvePublication(id: String): Boolean = updatePending(id) {
        it.copy(status = PublicationStatus.VERIFIED, rejectionReason = null)
    }

    override suspend fun rejectPublication(id: String, reason: String): Boolean {
        if (reason.isBlank()) return false
        return updatePending(id) {
            it.copy(status = PublicationStatus.REJECTED, rejectionReason = reason.trim())
        }
    }

    override suspend fun toggleInterest(id: String) = update(id) {
        it.copy(interested = !it.interested, votes = it.votes + if (it.interested) -1 else 1)
    }

    override suspend fun toggleVisited(id: String) = update(id) { it.copy(visited = !it.visited) }

    private fun update(id: String, transform: (Publication) -> Publication) {
        publications.update { list -> list.map { if (it.id == id) transform(it) else it } }
    }

    private fun updatePending(id: String, transform: (Publication) -> Publication): Boolean {
        while (true) {
            val current = publications.value
            val target = current.firstOrNull { it.id == id && it.status == PublicationStatus.PENDING }
                ?: return false
            val updated = current.map { publication ->
                if (publication.id == target.id) transform(publication) else publication
            }
            if (publications.compareAndSet(current, updated)) return true
        }
    }
}
