package com.app.rumbo.data.remote

import androidx.annotation.Keep
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.RoutePriority
import com.app.rumbo.domain.model.SavedRoute
import com.app.rumbo.domain.model.SavedRouteStop
import com.app.rumbo.domain.model.Transport

// Representación de una SavedRoute tal como se guarda en Firestore.
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject().
@Keep // Firestore llena esta clase buscando sus atributos por nombre
data class SavedRouteDto(
    val id: String = "",
    val name: String = "",
    val savedAt: Long = 0L,
    val distanceKm: Double = 0.0,
    val totalMinutes: Int = 0,
    val hours: Int = 0,
    val budget: String = PriceLevel.FREE.name,
    val categories: List<String> = emptyList(),
    val transport: String = Transport.CAR.name,
    val priority: String = RoutePriority.RELEVANT.name,
    val stops: List<SavedRouteStopDto> = emptyList()
) {
    fun toDomain(): SavedRoute = SavedRoute(
        id = id,
        name = name,
        savedAt = savedAt,
        stops = stops.map { it.toDomain() },
        distanceKm = distanceKm,
        totalMinutes = totalMinutes,
        hours = hours,
        budget = runCatching { PriceLevel.valueOf(budget) }.getOrDefault(PriceLevel.FREE),
        categories = categories.mapNotNull { runCatching { Category.valueOf(it) }.getOrNull() }.toSet(),
        transport = runCatching { Transport.valueOf(transport) }.getOrDefault(Transport.CAR),
        priority = runCatching { RoutePriority.valueOf(priority) }.getOrDefault(RoutePriority.RELEVANT)
    )

    companion object {
        fun fromDomain(route: SavedRoute): SavedRouteDto = SavedRouteDto(
            id = route.id,
            name = route.name,
            savedAt = route.savedAt,
            distanceKm = route.distanceKm,
            totalMinutes = route.totalMinutes,
            hours = route.hours,
            budget = route.budget.name,
            categories = route.categories.map { it.name },
            transport = route.transport.name,
            priority = route.priority.name,
            stops = route.stops.map { SavedRouteStopDto.fromDomain(it) }
        )
    }
}

@Keep // Firestore llena esta clase buscando sus atributos por nombre
data class SavedRouteStopDto(
    val publicationId: String = "",
    val title: String = "",
    val category: String = Category.GASTRONOMY.name,
    val price: String = PriceLevel.FREE.name,
    val town: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    fun toDomain(): SavedRouteStop = SavedRouteStop(
        publicationId = publicationId,
        title = title,
        category = runCatching { Category.valueOf(category) }.getOrDefault(Category.GASTRONOMY),
        price = runCatching { PriceLevel.valueOf(price) }.getOrDefault(PriceLevel.FREE),
        town = town,
        latitude = latitude,
        longitude = longitude
    )

    companion object {
        fun fromDomain(stop: SavedRouteStop): SavedRouteStopDto = SavedRouteStopDto(
            publicationId = stop.publicationId,
            title = stop.title,
            category = stop.category.name,
            price = stop.price.name,
            town = stop.town,
            latitude = stop.latitude,
            longitude = stop.longitude
        )
    }
}
