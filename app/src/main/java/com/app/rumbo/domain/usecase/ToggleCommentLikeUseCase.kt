package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Marca o desmarca "me gusta" en un comentario; solo en publicaciones verificadas. */
class ToggleCommentLikeUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    /** @return `false` si la publicación o el comentario no existen, o la publicación no está verificada. */
    suspend operator fun invoke(publicationId: String, commentId: String): Boolean {
        val publication = repository.observePublication(publicationId).first() ?: return false
        if (publication.status != PublicationStatus.VERIFIED) return false
        if (publication.comments.none { it.id == commentId }) return false
        repository.toggleCommentLike(publicationId, commentId)
        return true
    }
}
