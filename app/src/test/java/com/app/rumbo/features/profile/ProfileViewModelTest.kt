package com.app.rumbo.features.profile

import com.app.rumbo.domain.model.ActivityStats
import com.app.rumbo.domain.usecase.GetProfileUseCase
import com.app.rumbo.testutil.FakeActivityRepository
import com.app.rumbo.testutil.FakeUserRepository
import com.app.rumbo.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val activity = FakeActivityRepository()

    private fun createViewModel(user: FakeUserRepository = FakeUserRepository()) =
        ProfileViewModel(GetProfileUseCase(user, activity))

    private fun TestScope.loadedState(viewModel: ProfileViewModel): ProfileUiState {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        return viewModel.uiState.value
    }

    @Test
    fun `muestra el nombre las iniciales y el nivel del usuario`() = runTest {
        val state = loadedState(createViewModel())

        assertFalse(state.isLoading)
        assertEquals("Mariana Ospina", state.name)
        assertEquals("MO", state.initials)
        assertEquals(1, state.levelNumber)
        assertEquals("Turista", state.levelName)
    }

    @Test
    fun `calcula puntos siguiente nivel y progreso`() = runTest {
        activity.stats.value = ActivityStats(visitedPlaces = 1) // Primera visita: 10 puntos

        val state = loadedState(createViewModel())

        assertEquals(10, state.points)
        assertEquals("Exploradora", state.nextLevelName)
        assertEquals(150, state.nextLevelPoints)
        assertEquals(140, state.pointsToNext)
        assertEquals(10f / 150f, state.levelProgress, 0.001f)
    }

    @Test
    fun `separa las insignias ganadas de las bloqueadas`() = runTest {
        activity.stats.value = ActivityStats(visitedPlaces = 1)

        val state = loadedState(createViewModel())

        assertEquals(1, state.unlockedBadges.size)
        assertEquals(13, state.lockedBadgesCount)
        assertEquals(14, state.totalBadges)
    }

    @Test
    fun `los contadores reflejan la actividad`() = runTest {
        activity.stats.value = ActivityStats(publications = 3, pendingPublications = 1, visitedPlaces = 2)

        val state = loadedState(createViewModel())

        assertEquals(3, state.publicationsCount)
        assertEquals(1, state.pendingPublicationsCount)
        assertEquals(2, state.visitedCount)
        assertEquals(0, state.routesCount)
    }

    @Test
    fun `el perfil se actualiza cuando cambia la actividad`() = runTest {
        val viewModel = createViewModel()
        assertEquals(0, loadedState(viewModel).points)

        activity.stats.value = ActivityStats(publications = 1)

        assertEquals(25, viewModel.uiState.value.points)
    }

    @Test
    fun `en el nivel maximo no hay siguiente nivel`() = runTest {
        activity.stats.value = ActivityStats(
            publications = 25, verifiedPublications = 25, visitedPlaces = 50,
            commentsMade = 50, votesReceived = 200, routesCreated = 5, routesCompleted = 1,
            monthlyTopPublications = 1
        )

        val state = loadedState(createViewModel())

        assertEquals("Embajadora Local", state.levelName)
        assertNull(state.nextLevelName)
        assertEquals(1f, state.levelProgress, 0.001f)
    }

    @Test
    fun `sin usuario deja de cargar y queda vacio`() = runTest {
        val state = loadedState(createViewModel(FakeUserRepository(user = null)))

        assertFalse(state.isLoading)
        assertEquals("", state.name)
        assertTrue(state.unlockedBadges.isEmpty())
    }
}
