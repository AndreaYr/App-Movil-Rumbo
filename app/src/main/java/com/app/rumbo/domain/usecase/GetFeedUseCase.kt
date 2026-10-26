package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.FeedSort
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Feed público: solo publicaciones verificadas, filtradas por categoría y ordenadas según [sort].
 */
class GetFeedUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    operator fun invoke(category: Category?, sort: FeedSort): Flow<List<Publication>> =
        repository.observePublications().map { publications ->
            publications
                .filter { it.status == PublicationStatus.VERIFIED }
                .filter { category == null || it.category == category }
                .let { list ->
                    when (sort) {
                        FeedSort.RECENT -> list.sortedByDescending { it.publishedAt }
                        FeedSort.MOST_VOTED -> list.sortedByDescending { it.votes }
                        FeedSort.NEAREST -> list.sortedBy { it.distanceKm }
                    }
                }
        }
}
