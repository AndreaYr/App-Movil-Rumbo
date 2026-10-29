package com.app.rumbo.domain.usecase

import com.app.rumbo.data.local.SamplePublications
import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.model.RoutePreferences
import com.app.rumbo.domain.model.RoutePriority
import com.app.rumbo.domain.model.Transport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanRouteUseCaseTest {

    private val useCase = PlanRouteUseCase(InMemoryPublicationRepository())
    private val publications = SamplePublications.create()
    private val allCategories = Category.entries.toSet()

    private fun plan(preferences: RoutePreferences) = useCase.plan(publications, preferences)

    @Test
    fun `solo incluye publicaciones verificadas de las categorias elegidas`() {
        val preferences = RoutePreferences(hours = 12, budget = PriceLevel.EXPENSIVE, categories = setOf(Category.NATURE))

        val stops = plan(preferences).stops

        assertTrue(stops.isNotEmpty())
        assertTrue(stops.all { it.status == PublicationStatus.VERIFIED && it.category == Category.NATURE })
    }

    @Test
    fun `respeta el presupuesto maximo por persona`() {
        val preferences = RoutePreferences(hours = 12, budget = PriceLevel.FREE, categories = allCategories)

        val stops = plan(preferences).stops

        assertTrue(stops.isNotEmpty())
        assertTrue(stops.all { it.price == PriceLevel.FREE })
    }

    @Test
    fun `sin categorias no hay ruta`() {
        val plan = plan(RoutePreferences(categories = emptySet()))

        assertTrue(plan.stops.isEmpty())
        assertEquals(0, plan.totalMinutes)
        assertEquals(0.0, plan.distanceKm, 0.0)
    }

    @Test
    fun `la ruta nunca supera el tiempo disponible`() {
        Transport.entries.forEach { transport ->
            (RoutePreferences.MIN_HOURS..RoutePreferences.MAX_HOURS).forEach { hours ->
                val preferences = RoutePreferences(
                    hours = hours, budget = PriceLevel.EXPENSIVE, categories = allCategories, transport = transport
                )

                assertTrue("$transport $hours h", plan(preferences).totalMinutes <= hours * 60)
            }
        }
    }

    @Test
    fun `con mas tiempo caben igual o mas paradas`() {
        val base = RoutePreferences(budget = PriceLevel.EXPENSIVE, categories = allCategories)

        val short = plan(base.copy(hours = 2)).stops.size
        val long = plan(base.copy(hours = 12)).stops.size

        assertTrue(long >= short)
        assertTrue(long > 1)
    }

    @Test
    fun `un transporte mas rapido no reduce las paradas`() {
        val base = RoutePreferences(hours = 6, budget = PriceLevel.EXPENSIVE, categories = allCategories)

        val walking = plan(base.copy(transport = Transport.WALK)).stops.size
        val driving = plan(base.copy(transport = Transport.CAR)).stops.size

        assertTrue(driving >= walking)
    }

    @Test
    fun `la primera parada es la mas cercana al usuario`() {
        val preferences = RoutePreferences(hours = 12, budget = PriceLevel.EXPENSIVE, categories = allCategories)

        val stops = plan(preferences).stops

        assertEquals(stops.minOf { it.distanceKm }, stops.first().distanceKm, 0.0)
    }

    @Test
    fun `priorizar cercania elige primero los lugares mas cercanos`() {
        val preferences = RoutePreferences(
            hours = 2, budget = PriceLevel.EXPENSIVE, categories = allCategories,
            transport = Transport.CAR, priority = RoutePriority.NEAREST
        )

        val stops = plan(preferences).stops

        val nearestVerified = publications
            .filter { it.status == PublicationStatus.VERIFIED }
            .minBy { it.distanceKm }
        assertEquals(nearestVerified.id, stops.first().id)
    }

    @Test
    fun `priorizar relevancia incluye el lugar con mas votos si cabe`() {
        val preferences = RoutePreferences(
            hours = 12, budget = PriceLevel.EXPENSIVE, categories = allCategories,
            transport = Transport.CAR, priority = RoutePriority.RELEVANT
        )

        val stops = plan(preferences).stops

        val mostVoted = publications.filter { it.status == PublicationStatus.VERIFIED }.maxBy { it.votes }
        assertTrue(stops.any { it.id == mostVoted.id })
    }

    @Test
    fun `la distancia total incluye al menos el primer tramo desde el usuario`() {
        val preferences = RoutePreferences(hours = 12, budget = PriceLevel.EXPENSIVE, categories = allCategories)

        val plan = plan(preferences)

        assertTrue(plan.distanceKm >= plan.stops.first().distanceKm)
    }
}
