package com.app.rumbo.domain.repository

import com.app.rumbo.domain.model.PlaceResult

interface PlaceSearchRepository {
    /** Busca lugares y direcciones reales; devuelve una lista vacía si no hay resultados o falla la red. */
    suspend fun search(query: String, nearLatitude: Double, nearLongitude: Double): List<PlaceResult>

    /** Dirección más cercana a unas coordenadas; null si no hay ninguna o falla la red. */
    suspend fun reverse(latitude: Double, longitude: Double): PlaceResult?
}
