package com.app.rumbo.domain.model

/** Lugar o dirección real encontrado por el buscador de mapas. */
data class PlaceResult(
    val id: String,
    val name: String,
    /** Dirección o contexto (municipio, departamento, país). */
    val address: String,
    val latitude: Double,
    val longitude: Double
) {
    /** Texto listo para mostrar o guardar como dirección: "Calle 5 #3-20, Calarcá, Quindío". */
    val fullAddress: String get() = if (address.isBlank()) name else "$name, $address"
}
