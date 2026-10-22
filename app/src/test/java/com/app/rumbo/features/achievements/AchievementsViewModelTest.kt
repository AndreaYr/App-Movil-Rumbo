package com.app.rumbo.features.achievements

import com.app.rumbo.domain.model.Achievement
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AchievementsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val activity = FakeActivityRepository()

    private fun createViewModel(user: FakeUserRepository = FakeUserRepository()) =
        AchievementsViewModel(GetProfileUseCase(user, activity))

    private fun TestScope.loadedState(viewModel: AchievementsViewModel): AchievementsUiState {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        return viewModel.uiState.value
    }

    @Test
    fun `sin actividad todos los logros estan por desbloquear`() = runTest {
        val state = loadedState(createViewModel())

        assertFalse(state.isLoading)
        assertTrue(state.unlocked.isEmpty())
        assertEquals(14, state.locked.size)
        assertEquals(0, state.pointsEarned)
        assertEquals(0f, state.unlockedProgress, 0.001f)
    }

    @Test
    fun `separa desbloqueados y bloqueados y suma sus puntos`() = runTest {
        activity.stats.value = ActivityStats(publications = 1, visitedPlaces = 1)

        val state = loadedState(createViewModel())

        assertEquals(
            setOf(Achievement.FIRST_PUBLICATION, Achievement.FIRST_VISIT),
            state.unlocked.map { it.achievement }.toSet()
        )
        assertEquals(12, state.locked.size)
        assertEquals(14, state.total)
        assertEquals(35, state.pointsEarned)
        assertEquals(2f / 14f, state.unlockedProgress, 0.001f)
    }

    @Test
    fun `los bloqueados conservan su avance`() = runTest {
        activity.stats.value = ActivityStats(verifiedPublications = 6)

        val tenVerified = loadedState(createViewModel()).locked.first { it.achievement == Achievement.TEN_VERIFIED }

        assertEquals(6, tenVerified.current)
        assertEquals(0.6f, tenVerified.progress, 0.001f)
    }

    @Test
    fun `sin usuario deja de cargar y queda vacio`() = runTest {
        val state = loadedState(createViewModel(FakeUserRepository(user = null)))

        assertFalse(state.isLoading)
        assertEquals(0, state.total)
        assertEquals(0f, state.unlockedProgress, 0.001f)
    }
}
