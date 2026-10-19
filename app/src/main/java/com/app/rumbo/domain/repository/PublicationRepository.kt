package com.app.rumbo.domain.repository

import com.app.rumbo.domain.model.Publication
import kotlinx.coroutines.flow.Flow

interface PublicationRepository {
    fun observePublications(): Flow<List<Publication>>
    fun observePublication(id: String): Flow<Publication?>
    suspend fun addPublication(publication: Publication)
    suspend fun toggleInterest(id: String)
    suspend fun toggleVisited(id: String)
}
