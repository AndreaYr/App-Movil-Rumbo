package com.app.rumbo.domain.model

data class Comment(
    val id: String,
    val author: Author,
    val text: String,
    val publishedAt: Long,
    val likes: Int,
    /** El usuario actual marcó este comentario con "me gusta". */
    val liked: Boolean = false
)
