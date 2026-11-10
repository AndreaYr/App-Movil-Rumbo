package com.app.rumbo.domain.model

/** Punto de inicio elegido en el mapa; si no hay uno, la ruta sale de la ubicación del usuario. */
data class StartPoint(
    val label: String,
    val latitude: Double,
    val longitude: Double
)
