package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.RoutePlan
import com.app.rumbo.domain.model.RoutePreferences
import com.app.rumbo.domain.model.toSavedRoute
import com.app.rumbo.domain.repository.SavedRouteRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

/** Guarda la ruta con todos sus datos. Si esa misma ruta ya está guardada, no la duplica. */
class SaveRouteUseCase @Inject constructor(
    private val repository: SavedRouteRepository
) {
    suspend operator fun invoke(
        plan: RoutePlan,
        preferences: RoutePreferences,
        now: Long = System.currentTimeMillis()
    ) {
        if (plan.stops.isEmpty()) return
        val route = plan.toSavedRoute(preferences, id = UUID.randomUUID().toString(), savedAt = now)
        if (repository.observeRoutes().first().none { it.isSameRouteAs(route) }) {
            repository.saveRoute(route)
        }
    }
}
