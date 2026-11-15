package com.app.rumbo.data.remote

import androidx.annotation.Keep
import com.app.rumbo.domain.model.Author

// Autor embebido dentro del documento de una publicación.
@Keep // Firestore llena esta clase buscando sus atributos por nombre
data class AuthorDto(
    val id: String = "",
    val name: String = "",
    val levelName: String = ""
) {
    fun toDomain(): Author = Author(
        id = id,
        name = name,
        levelName = levelName
    )

    companion object {
        fun fromDomain(author: Author): AuthorDto = AuthorDto(
            id = author.id,
            name = author.name,
            levelName = author.levelName
        )
    }
}
