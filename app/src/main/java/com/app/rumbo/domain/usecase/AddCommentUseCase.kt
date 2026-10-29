package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.Author
import com.app.rumbo.domain.model.Comment
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

const val MAX_COMMENT_LENGTH = 300

/** Publica un comentario del usuario; solo las publicaciones verificadas reciben comentarios. */
class AddCommentUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    /** @return `false` si el texto está vacío o la publicación no existe o no está verificada. */
    suspend operator fun invoke(
        publicationId: String,
        text: String,
        author: Author,
        now: Long = System.currentTimeMillis()
    ): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return false
        val publication = repository.observePublication(publicationId).first() ?: return false
        if (publication.status != PublicationStatus.VERIFIED) return false
        repository.addComment(
            publicationId,
            Comment(
                id = UUID.randomUUID().toString(),
                author = author,
                text = trimmed.take(MAX_COMMENT_LENGTH),
                publishedAt = now,
                likes = 0
            )
        )
        return true
    }
}
