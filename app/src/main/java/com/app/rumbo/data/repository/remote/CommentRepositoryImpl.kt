package com.app.rumbo.data.repository.remote

import com.app.rumbo.data.remote.CommentDto
import com.app.rumbo.data.remote.PublicationDto
import com.app.rumbo.domain.model.Comment
import com.app.rumbo.domain.repository.CommentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda los comentarios dentro del documento de su publicación en Firestore
 * (colección "publications", campo "comments").
 */
@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CommentRepository {

    private val collection = firestore.collection("publications")

    override fun observeComments(publicationId: String): Flow<List<Comment>> = callbackFlow {
        val registration = collection.document(publicationId).addSnapshotListener { snapshot, _ ->
            val comments = snapshot
                ?.toObject(PublicationDto::class.java)
                ?.comments
                ?.map { it.toDomain() }
                ?: emptyList()
            trySend(comments)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun addComment(publicationId: String, comment: Comment) =
        updateComments(publicationId) { comments ->
            listOf(CommentDto.fromDomain(comment)) + comments
        }

    override suspend fun toggleCommentLike(publicationId: String, commentId: String) =
        updateComments(publicationId) { comments ->
            comments.map {
                if (it.id == commentId) {
                    it.copy(liked = !it.liked, likes = it.likes + if (it.liked) -1 else 1)
                } else {
                    it
                }
            }
        }

    /** Actualiza el arreglo de comentarios dentro de una transacción para no pisar cambios simultáneos. */
    private suspend fun updateComments(
        publicationId: String,
        transform: (List<CommentDto>) -> List<CommentDto>
    ) {
        val reference = collection.document(publicationId)
        firestore.runTransaction { transaction ->
            val dto = transaction.get(reference).toObject(PublicationDto::class.java)
                ?: return@runTransaction
            transaction.update(reference, "comments", transform(dto.comments))
        }.await()
    }
}
