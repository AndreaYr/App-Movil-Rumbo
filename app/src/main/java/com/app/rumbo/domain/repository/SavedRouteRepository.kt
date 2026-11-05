package com.app.rumbo.domain.repository

import com.app.rumbo.domain.model.SavedRoute
import kotlinx.coroutines.flow.Flow

interface SavedRouteRepository {
    /** Rutas guardadas, de la más reciente a la más antigua. */
    fun observeRoutes(): Flow<List<SavedRoute>>

    /** @throws java.io.IOException si no se pudo escribir en el almacenamiento. */
    suspend fun saveRoute(route: SavedRoute)
}
