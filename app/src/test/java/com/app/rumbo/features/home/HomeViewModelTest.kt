package com.app.rumbo.features.home

import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.data.repository.InMemoryUserRepository
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.usecase.GetCurrentUserUseCase
import com.app.rumbo.domain.usecase.GetHomeUseCase
import com.app.rumbo.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        viewModel = HomeViewModel(
            GetHomeUseCase(InMemoryPublicationRepository()),
            GetCurrentUserUseCase(InMemoryUserRepository())
        )
    }

    private fun TestScope.loadedState(): HomeUiState {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        return viewModel.uiState.value
    }

    @Test
    fun `el saludo muestra el nombre y la ciudad del usuario`() = runTest {
        val state = loadedState()

        assertFalse(state.isLoading)
        assertEquals("Mariana", state.userName)
        assertEquals("Armenia, Quindío", state.city)
    }

    @Test
    fun `cerca de ti trae 3 verificadas de la mas cercana a la mas lejana`() = runTest {
        val nearby = loadedState().content.nearby

        assertEquals(3, nearby.size)
        assertTrue(nearby.all { it.status == PublicationStatus.VERIFIED })
        assertEquals(nearby.sortedBy { it.distanceKm }, nearby)
    }

    @Test
    fun `mas destacados trae las 2 verificadas con mas votos`() = runTest {
        val top = loadedState().content.topRated

        assertEquals(listOf("parque-cafe", "valle-cocora"), top.map { it.id })
    }

    @Test
    fun `tendencias calcula el crecimiento semanal y ordena de mayor a menor`() = runTest {
        val trending = loadedState().content.trending

        assertEquals(3, trending.size)
        // Museo del Oro: 250 votos esta semana sobre 388 previos = 64 %
        assertEquals("museo-oro-quimbaya", trending[0].publication.id)
        assertEquals(64, trending[0].growthPercent)
        // Trucha del Cocora: 121 sobre 297 previos = 40 %
        assertEquals("trucha-cocora", trending[1].publication.id)
        assertEquals(40, trending[1].growthPercent)
    }

    @Test
    fun `nuevos lugares trae las 2 mas recientes y deja fuera las rechazadas`() = runTest {
        val fresh = loadedState().content.fresh

        assertEquals(listOf("valle-cocora", "trucha-cocora"), fresh.map { it.id })
        assertTrue(fresh.none { it.status == PublicationStatus.REJECTED })
    }
}
