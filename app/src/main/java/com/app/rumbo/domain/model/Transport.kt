package com.app.rumbo.domain.model

import androidx.annotation.StringRes
import com.app.rumbo.R

/** Medio de transporte del planificador; [speedKmh] es la velocidad media para estimar los trayectos. */
enum class Transport(@param:StringRes val labelRes: Int, val speedKmh: Double) {
    WALK(R.string.transport_walk, 4.5),
    BIKE(R.string.transport_bike, 14.0),
    MOTO(R.string.transport_moto, 35.0),
    CAR(R.string.transport_car, 40.0)
}
