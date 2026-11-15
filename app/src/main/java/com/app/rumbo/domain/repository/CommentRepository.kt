package com.app.rumbo.domain.repository

import com.app.rumbo.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    /** Comentarios de una publicación, del más reciente al más antiguo. */
    fun observeComments(publicationId: String): Flow<List<Comment>>

    suspend fun addComment(publicationId: String, comment: Comment)

    /** Marca o desmarca el "me gusta" del usuario en un comentario. */
    suspend fun toggleCommentLike(publicationId: String, commentId: String)
}
