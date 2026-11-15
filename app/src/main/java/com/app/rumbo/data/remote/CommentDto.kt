package com.app.rumbo.data.remote

import androidx.annotation.Keep
import com.app.rumbo.domain.model.Comment


@Keep // Firestore llena esta clase buscando sus atributos por nombre
data class CommentDto(
    val id: String = "",
    val author: AuthorDto = AuthorDto(),
    val text: String = "",
    val publishedAt: Long = 0L,
    val likes: Int = 0,
    val liked: Boolean = false
) {
    fun toDomain(): Comment = Comment(
        id = id,
        author = author.toDomain(),
        text = text,
        publishedAt = publishedAt,
        likes = likes,
        liked = liked
    )

    companion object {
        fun fromDomain(comment: Comment): CommentDto = CommentDto(
            id = comment.id,
            author = AuthorDto.fromDomain(comment.author),
            text = comment.text,
            publishedAt = comment.publishedAt,
            likes = comment.likes,
            liked = comment.liked
        )
    }
}
