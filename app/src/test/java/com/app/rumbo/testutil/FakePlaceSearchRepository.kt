package com.app.rumbo.testutil

import com.app.rumbo.domain.model.PlaceResult
import com.app.rumbo.domain.repository.PlaceSearchRepository

/** Repositorio de lugares para pruebas: guarda las consultas recibidas y devuelve [results]. */
class FakePlaceSearchRepository(private val results: List<PlaceResult> = emptyList()) : PlaceSearchRepository {
    val queries = mutableListOf<String>()

    override suspend fun search(query: String, nearLatitude: Double, nearLongitude: Double): List<PlaceResult> {
        queries += query
        return results
    }
}
