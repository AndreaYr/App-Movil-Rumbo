package com.app.rumbo.features.planroute

import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.PlaceResult
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.RoutePreferences
import com.app.rumbo.domain.model.RoutePriority
import com.app.rumbo.domain.model.Transport
import com.app.rumbo.domain.usecase.GetCurrentUserUseCase
import com.app.rumbo.domain.usecase.GetSavedRoutesUseCase
import com.app.rumbo.domain.usecase.PlanRouteUseCase
import com.app.rumbo.domain.usecase.ReverseGeocodeUseCase
import com.app.rumbo.domain.usecase.SaveRouteUseCase
import com.app.rumbo.domain.usecase.SearchPlacesUseCase
import com.app.rumbo.testutil.FakePlaceSearchRepository
import com.app.rumbo.testutil.FakeSavedRouteRepository
import com.app.rumbo.testutil.FakeUserRepository
import com.app.rumbo.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlanRouteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: PlanRouteViewModel

    private val savedRoutes = FakeSavedRouteRepository()
    private val places = FakePlaceSearchRepository(
        reverseResult = PlaceResult("1", "Carrera 24 #40-15", "Calarcá, Quindío", 4.52, -75.64)
    )

    @Before
    fun setUp() {
        viewModel = PlanRouteViewModel(
            PlanRouteUseCase(InMemoryPublicationRepository()),
            GetCurrentUserUseCase(FakeUserRepository()),
            GetSavedRoutesUseCase(savedRoutes),
            SaveRouteUseCase(savedRoutes),
            SearchPlacesUseCase(places),
            ReverseGeocodeUseCase(places)
        )
    }

    private fun TestScope.loadedState(): PlanRouteUiState {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        return viewModel.uiState.value
    }

    @Test
    fun `arranca con los parametros del prototipo y la ciudad del usuario`() = runTest {
        val state = loadedState()

        assertFalse(state.isLoading)
        assertEquals("Armenia, Quindío", state.city)
        assertEquals(RoutePreferences(), state.preferences)
        assertEquals(6, state.preferences.hours)
        assertEquals(PriceLevel.MODERATE, state.preferences.budget)
        assertEquals(setOf(Category.NATURE, Category.GASTRONOMY), state.preferences.categories)
        assertEquals(Transport.WALK, state.preferences.transport)
        assertEquals(RoutePriority.RELEVANT, state.preferences.priority)
    }

    @Test
    fun `el tiempo se limita al rango permitido`() = runTest {
        loadedState()

        viewModel.onHoursChange(99)
        assertEquals(RoutePreferences.MAX_HOURS, viewModel.uiState.value.preferences.hours)

        viewModel.onHoursChange(0)
        assertEquals(RoutePreferences.MIN_HOURS, viewModel.uiState.value.preferences.hours)
    }

    @Test
    fun `cambiar presupuesto transporte y prioridad actualiza las preferencias`() = runTest {
        loadedState()

        viewModel.onBudgetChange(PriceLevel.FREE)
        viewModel.onTransportChange(Transport.MOTO)
        viewModel.onPriorityChange(RoutePriority.NEAREST)

        val preferences = viewModel.uiState.value.preferences
        assertEquals(PriceLevel.FREE, preferences.budget)
        assertEquals(Transport.MOTO, preferences.transport)
        assertEquals(RoutePriority.NEAREST, preferences.priority)
    }

    @Test
    fun `tocar una categoria la agrega o la quita`() = runTest {
        loadedState()

        viewModel.onCategoryToggle(Category.CULTURE)
        assertTrue(Category.CULTURE in viewModel.uiState.value.preferences.categories)

        viewModel.onCategoryToggle(Category.CULTURE)
        assertFalse(Category.CULTURE in viewModel.uiState.value.preferences.categories)
    }

    @Test
    fun `la ruta se recalcula al cambiar un parametro`() = runTest {
        loadedState()
        viewModel.onBudgetChange(PriceLevel.EXPENSIVE)
        viewModel.onTransportChange(Transport.CAR)
        viewModel.onHoursChange(12)
        Category.entries.forEach { category ->
            if (category !in viewModel.uiState.value.preferences.categories) viewModel.onCategoryToggle(category)
        }
        val full = viewModel.uiState.value.plan.stops.size

        Category.entries.forEach { viewModel.onCategoryToggle(it) }

        assertTrue(full > 0)
        assertTrue(viewModel.uiState.value.plan.stops.isEmpty())
    }

    @Test
    fun `elegir un punto en el mapa detecta su direccion y se confirma como inicio`() = runTest {
        loadedState()

        viewModel.onOpenStartPicker()
        viewModel.onStartMapPoint(4.52, -75.64)
        advanceUntilIdle()

        val picker = viewModel.pickerState.value
        assertTrue(picker.isOpen)
        assertEquals("Carrera 24 #40-15, Calarcá, Quindío", picker.point?.label)

        viewModel.onConfirmStart()

        assertFalse(viewModel.pickerState.value.isOpen)
        assertEquals(4.52, viewModel.uiState.value.preferences.start?.latitude ?: 0.0, 0.0)
    }

    @Test
    fun `volver a mi ubicacion quita el punto de inicio elegido`() = runTest {
        loadedState()
        viewModel.onOpenStartPicker()
        viewModel.onStartMapPoint(4.52, -75.64)
        viewModel.onConfirmStart()

        viewModel.onUseMyLocationAsStart()

        assertEquals(null, viewModel.uiState.value.preferences.start)
    }

    @Test
    fun `cerrar la ventana sin confirmar no cambia el inicio`() = runTest {
        loadedState()
        viewModel.onOpenStartPicker()
        viewModel.onStartMapPoint(4.52, -75.64)

        viewModel.onCloseStartPicker()

        assertEquals(null, viewModel.uiState.value.preferences.start)
        assertFalse(viewModel.pickerState.value.isOpen)
    }
}
