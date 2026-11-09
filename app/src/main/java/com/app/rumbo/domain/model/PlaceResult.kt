package com.app.rumbo.domain.model

/** Lugar o dirección real encontrado por el buscador de mapas. */
data class PlaceResult(
    val id: String,
    val name: String,
    /** Dirección o contexto (municipio, departamento, país). */
    val address: String,
    val latitude: Double,
    val longitude: Double
)
