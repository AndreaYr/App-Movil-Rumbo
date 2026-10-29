package com.app.rumbo.domain.model

/** Criterio con el que el planificador elige las paradas cuando no caben todas. */
enum class RoutePriority(val label: String) {
    RELEVANT("Más relevantes"),
    NEAREST("Más cercanos")
}
