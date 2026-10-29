package com.app.rumbo.domain.repository

import com.app.rumbo.domain.model.Comment
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus
import kotlinx.coroutines.flow.Flow

interface PublicationRepository {
    fun observePublications(): Flow<List<Publication>>
    fun observePublication(id: String): Flow<Publication?>
    suspend fun addPublication(publication: Publication)
    suspend fun toggleInterest(id: String)
    suspend fun toggleVisited(id: String)

    /** Agrega el comentario al inicio de la lista de la publicación. */
    suspend fun addComment(publicationId: String, comment: Comment)

    /** Marca o desmarca el "me gusta" del usuario en un comentario. */
    suspend fun toggleCommentLike(publicationId: String, commentId: String)

    /** Cambia el estado; el motivo solo se conserva si la publicación queda [PublicationStatus.REJECTED]. */
    suspend fun updateStatus(
        id: String,
        status: PublicationStatus,
        rejectionReason: String? = null,
        rejectionDetail: String? = null
    )

    /** Reemplaza la publicación que tenga el mismo id. */
    suspend fun updatePublication(publication: Publication)
}
