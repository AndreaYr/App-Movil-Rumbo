package com.app.rumbo.domain.repository

import com.app.rumbo.domain.model.Author
import kotlinx.coroutines.flow.Flow

interface AuthorRepository {
    /** Autor (usuario) con el id indicado; null si la cuenta no existe. */
    fun observeAuthor(id: String): Flow<Author?>
}
