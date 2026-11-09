package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.PlaceResult
import com.app.rumbo.domain.repository.PlaceSearchRepository
import javax.inject.Inject

/** Busca lugares y direcciones reales, priorizando los cercanos a un punto. */
class SearchPlacesUseCase @Inject constructor(
    private val repository: PlaceSearchRepository
) {
    suspend operator fun invoke(query: String, nearLatitude: Double, nearLongitude: Double): List<PlaceResult> {
        val text = query.trim()
        return if (text.length < MIN_QUERY_LENGTH) emptyList() else repository.search(text, nearLatitude, nearLongitude)
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 3
    }
}
