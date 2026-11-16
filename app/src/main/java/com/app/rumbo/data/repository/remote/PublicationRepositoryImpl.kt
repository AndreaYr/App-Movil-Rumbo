package com.app.rumbo.data.repository.remote

import com.app.rumbo.data.remote.PublicationDto
import com.app.rumbo.domain.model.Comment
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.CommentRepository
import com.app.rumbo.domain.repository.PublicationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda las publicaciones en la colección "publications" de Firestore. Los comentarios y las
 * fotos viven dentro del mismo documento, igual que en [PublicationDto].
 *
 * "Me interesa" y "Visitado" son personales: se guardan como listas de ids en el documento del
 * usuario (`users/{uid}`) y se combinan con las publicaciones al leerlas. Lo único compartido es
 * el contador de votos de la publicación.
 */
@Singleton
class PublicationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val commentRepository: CommentRepository
) : PublicationRepository {

    private val collection = firestore.collection("publications")

    override fun observePublications(): Flow<List<Publication>> =
        combine(publicationsFlow(), observeUserPlaces()) { publications, places ->
            publications.map { it.withUserPlaces(places) }
        }

    override fun observePublication(id: String): Flow<Publication?> =
        combine(publicationFlow(id), observeUserPlaces()) { publication, places ->
            publication?.withUserPlaces(places)
        }

    private fun publicationsFlow(): Flow<List<Publication>> = callbackFlow {
        val registration = collection.addSnapshotListener { snapshot, _ ->
            val publications = snapshot?.documents
                ?.mapNotNull { it.toPublicationOrNull() }
                ?.sortedByDescending { it.publishedAt }
                ?: emptyList()
            trySend(publications)
        }
        awaitClose { registration.remove() }
    }

    private fun publicationFlow(id: String): Flow<Publication?> = callbackFlow {
        val registration = collection.document(id).addSnapshotListener { snapshot, _ ->
            trySend(snapshot?.toPublicationOrNull())
        }
        awaitClose { registration.remove() }
    }

    /** Lugares marcados por el usuario con sesión; vacío si no hay sesión. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeUserPlaces(): Flow<UserPlaces> = authStateFlow().flatMapLatest { uid ->
        if (uid == null) flowOf(UserPlaces()) else callbackFlow {
            val registration = firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    trySend(
                        UserPlaces(
                            interested = snapshot.idList(FIELD_INTERESTED),
                            visited = snapshot.idList(FIELD_VISITED)
                        )
                    )
                }
            awaitClose { registration.remove() }
        }
    }

    private fun authStateFlow(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    private fun DocumentSnapshot?.idList(field: String): Set<String> =
        (this?.get(field) as? List<*>).orEmpty().filterIsInstance<String>().toSet()

    private fun Publication.withUserPlaces(places: UserPlaces) = copy(
        interested = id in places.interested,
        visited = id in places.visited
    )

    private data class UserPlaces(
        val interested: Set<String> = emptySet(),
        val visited: Set<String> = emptySet()
    )

    override suspend fun addPublication(publication: Publication) {
        collection.document(publication.id)
            .set(PublicationDto.fromDomain(publication))
            .await()
    }

    override suspend fun approvePublication(id: String): Boolean = updatePending(id) { dto ->
        dto.copy(
            status = PublicationStatus.VERIFIED.name,
            rejectionReason = null,
            rejectionDetail = null,
            moderatedAt = System.currentTimeMillis(),
            moderatedBy = auth.currentUser?.uid.orEmpty()
        )
    }

    override suspend fun rejectPublication(id: String, reason: String, detail: String): Boolean {
        if (reason.isBlank()) return false
        return updatePending(id) { dto ->
            dto.copy(
                status = PublicationStatus.REJECTED.name,
                rejectionReason = reason.trim(),
                rejectionDetail = detail.trim(),
                moderatedAt = System.currentTimeMillis(),
                moderatedBy = auth.currentUser?.uid.orEmpty()
            )
        }
    }

    override suspend fun toggleInterest(id: String) = togglePlace(id, FIELD_INTERESTED, countsAsVote = true)

    override suspend fun toggleVisited(id: String) = togglePlace(id, FIELD_VISITED, countsAsVote = false)

    /**
     * Agrega o quita el id de la lista del usuario y, si es "Me interesa", ajusta el contador
     * de votos de la publicación. Todo en una transacción para que ambos cambios vayan juntos.
     */
    private suspend fun togglePlace(id: String, field: String, countsAsVote: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val userReference = firestore.collection("users").document(uid)
        val publicationReference = collection.document(id)
        firestore.runTransaction { transaction ->
            val userSnapshot = transaction.get(userReference)
            if (!transaction.get(publicationReference).exists() || !userSnapshot.exists()) {
                return@runTransaction
            }
            val isMarked = id in userSnapshot.idList(field)
            transaction.update(
                userReference,
                field,
                if (isMarked) FieldValue.arrayRemove(id) else FieldValue.arrayUnion(id)
            )
            if (countsAsVote) {
                transaction.update(publicationReference, "votes", FieldValue.increment(if (isMarked) -1L else 1L))
            }
        }.await()
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

    private companion object {
        const val FIELD_INTERESTED = "interestedIds"
        const val FIELD_VISITED = "visitedIds"
    }
}
