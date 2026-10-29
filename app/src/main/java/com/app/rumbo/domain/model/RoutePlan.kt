package com.app.rumbo.domain.model

/** Ruta calculada: paradas en el orden en que se recorren, con su distancia y duración totales. */
data class RoutePlan(
    val stops: List<Publication> = emptyList(),
    val distanceKm: Double = 0.0,
    /** Tiempo en las paradas más el de los trayectos entre ellas. */
    val totalMinutes: Int = 0
)
