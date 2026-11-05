package com.app.rumbo.domain.model

import kotlinx.serialization.Serializable

/**
 * Ruta guardada por el usuario. Conserva una copia completa de las paradas y los parámetros,
 * así se puede ver tal como se guardó aunque cambien las publicaciones.
 */
@Serializable
data class SavedRoute(
    val id: String,
    val name: String,
    val savedAt: Long,
    val stops: List<SavedRouteStop>,
    val distanceKm: Double,
    val totalMinutes: Int,
    val hours: Int,
    val budget: PriceLevel,
    val categories: Set<Category>,
    val transport: Transport,
    val priority: RoutePriority
) {
    /** Dos rutas son la misma si recorren las mismas paradas, en el mismo orden y con el mismo transporte. */
    fun isSameRouteAs(other: SavedRoute): Boolean =
        transport == other.transport &&
            stops.map { it.publicationId } == other.stops.map { it.publicationId }
}

@Serializable
data class SavedRouteStop(
    val publicationId: String,
    val title: String,
    val category: Category,
    val price: PriceLevel,
    val town: String,
    val latitude: Double,
    val longitude: Double
)

/** Convierte un plan calculado en la ruta que se muestra y se guarda. */
fun RoutePlan.toSavedRoute(
    preferences: RoutePreferences,
    id: String = "",
    savedAt: Long = 0L
): SavedRoute {
    val towns = stops.map { it.town }.distinct()
    return SavedRoute(
        id = id,
        name = when {
            towns.isEmpty() -> "Tu ruta"
            towns.size == 1 -> "Ruta por ${towns.first()}"
            else -> "Ruta por ${towns[0]} y ${towns[1]}"
        },
        savedAt = savedAt,
        stops = stops.map {
            SavedRouteStop(it.id, it.title, it.category, it.price, it.town, it.latitude, it.longitude)
        },
        distanceKm = distanceKm,
        totalMinutes = totalMinutes,
        hours = preferences.hours,
        budget = preferences.budget,
        categories = preferences.categories,
        transport = preferences.transport,
        priority = preferences.priority
    )
}
