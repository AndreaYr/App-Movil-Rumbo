package com.app.rumbo.domain.model

/** Parámetros de la pantalla "Planifica tu ruta"; los valores por defecto son los del prototipo. */
data class RoutePreferences(
    val hours: Int = DEFAULT_HOURS,
    /** Precio máximo por persona: se aceptan los lugares de ese nivel o más baratos. */
    val budget: PriceLevel = PriceLevel.MODERATE,
    val categories: Set<Category> = setOf(Category.NATURE, Category.GASTRONOMY),
    val transport: Transport = Transport.WALK,
    val priority: RoutePriority = RoutePriority.RELEVANT
) {
    companion object {
        const val MIN_HOURS = 2
        const val MAX_HOURS = 12
        const val DEFAULT_HOURS = 6
    }
}
