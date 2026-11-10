package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.PlaceResult
import com.app.rumbo.domain.repository.PlaceSearchRepository
import javax.inject.Inject

/** Obtiene la dirección de un punto del mapa. */
class ReverseGeocodeUseCase @Inject constructor(
    private val repository: PlaceSearchRepository
) {
    suspend operator fun invoke(latitude: Double, longitude: Double): PlaceResult? =
        repository.reverse(latitude, longitude)
}
