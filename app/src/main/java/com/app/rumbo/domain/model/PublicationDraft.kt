package com.app.rumbo.domain.model

/**
 * Datos que la comunidad diligencia en la pantalla "Crear punto de interés".
 * Es la entrada para dar de alta una nueva [Publication]; el id, el autor, la
 * fecha y el estado se asignan al publicar.
 */
data class PublicationDraft(
    val title: String,
    val description: String,
    val category: Category,
    val schedule: String,
    val price: PriceLevel,
    val latitude: Double,
    val longitude: Double,
    val photos: List<Photo>,
    val town: String = "",
    val address: String = "",
    val distanceKm: Double = 0.0
)
