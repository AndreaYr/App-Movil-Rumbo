package com.app.rumbo.domain.usecase

import com.app.rumbo.data.local.SamplePublications
import com.app.rumbo.domain.model.RoutePlan
import com.app.rumbo.domain.model.RoutePreferences
import com.app.rumbo.domain.model.SavedRoute
import com.app.rumbo.domain.model.Transport
import com.app.rumbo.testutil.FakeSavedRouteRepository
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveRouteUseCaseTest {

    private val repository = FakeSavedRouteRepository()
    private val saveRoute = SaveRouteUseCase(repository)
    private val stops = SamplePublications.create().take(2)
    private val plan = RoutePlan(stops = stops, distanceKm = 12.5, totalMinutes = 287)

    @Test
    fun `guarda paradas distancia tiempo y parametros`() = runTest {
        saveRoute(plan, RoutePreferences(hours = 8, transport = Transport.JEEP), now = 1_000L)

        val route = repository.routes.value.single()
        assertEquals(stops.map { it.id }, route.stops.map { it.publicationId })
        assertEquals(stops.first().latitude, route.stops.first().latitude, 0.0)
        assertEquals(12.5, route.distanceKm, 0.0)
        assertEquals(287, route.totalMinutes)
        assertEquals(8, route.hours)
        assertEquals(Transport.JEEP, route.transport)
        assertEquals(1_000L, route.savedAt)
    }

    @Test
    fun `no duplica la misma ruta`() = runTest {
        saveRoute(plan, RoutePreferences())
        saveRoute(plan, RoutePreferences())

        assertEquals(1, repository.routes.value.size)
    }

    @Test
    fun `la misma ruta con otro transporte se guarda aparte`() = runTest {
        saveRoute(plan, RoutePreferences(transport = Transport.WALK))
        saveRoute(plan, RoutePreferences(transport = Transport.CAR))

        assertEquals(2, repository.routes.value.size)
    }

    @Test
    fun `un plan sin paradas no se guarda`() = runTest {
        saveRoute(RoutePlan(), RoutePreferences())

        assertTrue(repository.routes.value.isEmpty())
    }

    @Test
    fun `la ruta sobrevive al formato del archivo`() = runTest {
        saveRoute(plan, RoutePreferences())
        val routes = repository.routes.value

        val restored = Json.decodeFromString<List<SavedRoute>>(Json.encodeToString(routes))

        assertEquals(routes, restored)
    }
}
