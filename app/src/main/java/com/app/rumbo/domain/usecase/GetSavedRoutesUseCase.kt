package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.SavedRoute
import com.app.rumbo.domain.repository.SavedRouteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedRoutesUseCase @Inject constructor(
    private val repository: SavedRouteRepository
) {
    operator fun invoke(): Flow<List<SavedRoute>> = repository.observeRoutes()
}
