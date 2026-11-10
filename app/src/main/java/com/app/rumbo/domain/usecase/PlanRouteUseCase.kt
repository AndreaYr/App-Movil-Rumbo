package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.model.RoutePlan
import com.app.rumbo.domain.model.RoutePreferences
import com.app.rumbo.domain.model.RoutePriority
import com.app.rumbo.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Arma la ruta del día con las publicaciones verificadas que cumplen las preferencias
 * (categoría y presupuesto). Se recorren en el orden de [RoutePreferences.priority] y cada una
 * entra solo si la ruta completa sigue cabiendo en el tiempo disponible; las paradas elegidas
 * se ordenan de la más cercana al usuario hacia la siguiente más próxima.
 */
class PlanRouteUseCase @Inject constructor(
    private val repository: PublicationRepository
) {
    operator fun invoke(preferences: RoutePreferences): Flow<RoutePlan> =
        repository.observePublications().map { plan(it, preferences) }

    fun plan(publications: List<Publication>, preferences: RoutePreferences): RoutePlan {
        val availableMinutes = preferences.hours * 60
        val ranked = publications
            .filter {
                it.status == PublicationStatus.VERIFIED &&
                    it.category in preferences.categories &&
                    it.price <= preferences.budget
            }
            .let { candidates ->
                when (preferences.priority) {
                    RoutePriority.RELEVANT -> candidates.sortedByDescending { it.votes }
                    RoutePriority.NEAREST -> candidates.sortedBy { distanceFromStart(it, preferences) }
                }
            }

        var best = RoutePlan()
        for (candidate in ranked) {
            if (best.stops.size >= MAX_STOPS) break
            val trial = route(best.stops + candidate, preferences)
            if (trial.totalMinutes <= availableMinutes) best = trial
        }
        return best
    }

    /** Ordena las paradas por cercanía y calcula distancia y tiempo. El primer tramo sale de la posición del usuario. */
    private fun route(stops: List<Publication>, preferences: RoutePreferences): RoutePlan {
        val remaining = stops.sortedBy { distanceFromStart(it, preferences) }.toMutableList()
        val ordered = mutableListOf(remaining.removeAt(0))
        var distanceKm = distanceFromStart(ordered.first(), preferences)
        while (remaining.isNotEmpty()) {
            val last = ordered.last()
            val next = remaining.minBy { kmBetween(last, it) }
            distanceKm += kmBetween(last, next)
            remaining.remove(next)
            ordered += next
        }
        val travelMinutes = distanceKm / preferences.transport.speedKmh * 60
        return RoutePlan(
            stops = ordered,
            distanceKm = distanceKm,
            totalMinutes = (travelMinutes + ordered.size * STAY_MINUTES).roundToInt()
        )
    }

    /**
     * Distancia desde donde sale la ruta hasta una parada. Con un punto de inicio elegido se mide en línea recta
     * desde él; si no, se usa la distancia al usuario que ya trae la publicación.
     */
    private fun distanceFromStart(publication: Publication, preferences: RoutePreferences): Double =
        preferences.start?.let {
            kmBetween(it.latitude, it.longitude, publication.latitude, publication.longitude)
        } ?: publication.distanceKm

    private fun kmBetween(a: Publication, b: Publication): Double =
        kmBetween(a.latitude, a.longitude, b.latitude, b.longitude)

    /** Distancia en línea recta entre dos puntos (fórmula del haversine). */
    private fun kmBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val h = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * EARTH_RADIUS_KM * asin(sqrt(h))
    }

    private companion object {
        const val MAX_STOPS = 8
        /** Tiempo medio que se pasa en cada parada. */
        const val STAY_MINUTES = 60
        const val EARTH_RADIUS_KM = 6371.0
    }
}
