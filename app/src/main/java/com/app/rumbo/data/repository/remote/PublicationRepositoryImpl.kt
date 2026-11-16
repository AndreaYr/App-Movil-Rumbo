package com.app.rumbo.data.repository.remote

import com.app.rumbo.data.remote.PublicationDto
import com.app.rumbo.domain.model.Comment
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.CommentRepository
import com.app.rumbo.domain.repository.PublicationRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda las publicaciones en la colección "publications" de Firestore. Los comentarios y las
 * fotos viven dentro del mismo documento, igual que en [PublicationDto].
 */
@Singleton
class PublicationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val commentRepository: CommentRepository
) : PublicationRepository {

    private val collection = firestore.collection("publications")

    override fun observePublications(): Flow<List<Publication>> = callbackFlow {
        val registration = collection.addSnapshotListener { snapshot, _ ->
            val publications = snapshot?.documents
                ?.mapNotNull { it.toPublicationOrNull() }
                ?.sortedByDescending { it.publishedAt }
                ?: emptyList()
            trySend(publications)
        }
        awaitClose { registration.remove() }
    }

    override fun observePublication(id: String): Flow<Publication?> = callbackFlow {
        val registration = collection.document(id).addSnapshotListener { snapshot, _ ->
            trySend(snapshot?.toPublicationOrNull())
        }
        awaitClose { registration.remove() }
    }

    override suspend fun addPublication(publication: Publication) {
        collection.document(publication.id)
            .set(PublicationDto.fromDomain(publication))
            .await()
    }

    override suspend fun approvePublication(id: String): Boolean = updatePending(id) { dto ->
        dto.copy(
            status = PublicationStatus.VERIFIED.name,
            rejectionReason = null,
            rejectionDetail = null
        )
    }

    override suspend fun rejectPublication(id: String, reason: String, detail: String): Boolean {
        if (reason.isBlank()) return false
        return updatePending(id) { dto ->
            dto.copy(
                status = PublicationStatus.REJECTED.name,
                rejectionReason = reason.trim(),
                rejectionDetail = detail.trim()
            )
        }
    }

    override suspend fun toggleInterest(id: String) = update(id) { dto ->
        dto.copy(
            interested = !dto.interested,
            votes = dto.votes + if (dto.interested) -1 else 1
        )
    }

    override suspend fun toggleVisited(id: String) = update(id) { dto ->
        dto.copy(visited = !dto.visited)
    }

    override suspend fun addComment(publicationId: String, comment: Comment) =
        commentRepository.addComment(publicationId, comment)

    override suspend fun toggleCommentLike(publicationId: String, commentId: String) =
        commentRepository.toggleCommentLike(publicationId, commentId)

    override suspend fun updateStatus(
        id: String,
        status: PublicationStatus,
        rejectionReason: String?,
        rejectionDetail: String?
    ) = update(id) { dto ->
        val rejected = status == PublicationStatus.REJECTED
        dto.copy(
            status = status.name,
            rejectionReason = if (rejected) rejectionReason else null,
            rejectionDetail = if (rejected) rejectionDetail else null
        )
    }

    override suspend fun updatePublication(publication: Publication) =
        update(publication.id) { PublicationDto.fromDomain(publication) }

    /** Aplica un cambio dentro de una transacción para no pisar escrituras simultáneas. */
    private suspend fun update(id: String, transform: (PublicationDto) -> PublicationDto) {
        val reference = collection.document(id)
        firestore.runTransaction { transaction ->
            val dto = transaction.get(reference).toObject(PublicationDto::class.java)
                ?: return@runTransaction
            transaction.set(reference, transform(dto.copy(id = reference.id)))
        }.await()
    }

    /** Igual que [update], pero solo si la publicación sigue en estado pendiente. */
    private suspend fun updatePending(
        id: String,
        transform: (PublicationDto) -> PublicationDto
    ): Boolean {
        val reference = collection.document(id)
        return firestore.runTransaction { transaction ->
            val dto = transaction.get(reference).toObject(PublicationDto::class.java)
            if (dto == null || dto.status != PublicationStatus.PENDING.name) {
                false
            } else {
                transaction.set(reference, transform(dto.copy(id = reference.id)))
                true
            }
        }.await()
    }

    private fun DocumentSnapshot.toPublicationOrNull(): Publication? = runCatching {
        toObject(PublicationDto::class.java)?.copy(id = id)?.toDomain()
    }.getOrNull()
}
