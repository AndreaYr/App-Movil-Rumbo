package com.app.rumbo.domain.model

/** Medio de transporte del planificador; [speedKmh] es la velocidad media para estimar los trayectos. */
enum class Transport(val label: String, val speedKmh: Double) {
    WALK("A pie", 4.5),
    BIKE("Bici", 14.0),
    CAR("Carro", 40.0),
    JEEP("Jeep", 25.0)
}
