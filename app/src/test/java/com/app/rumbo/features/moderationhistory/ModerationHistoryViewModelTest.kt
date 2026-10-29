package com.app.rumbo.features.moderationhistory

import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.usecase.GetModerationHistoryUseCase
import com.app.rumbo.domain.usecase.RejectPublicationUseCase
import com.app.rumbo.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ModerationHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = InMemoryPublicationRepository()

    private fun createViewModel() = ModerationHistoryViewModel(GetModerationHistoryUseCase(repository))

    @Test
    fun `carga todas las publicaciones moderadas`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        val state = viewModel.uiState.value
        assertTrue(state.publications.isNotEmpty())
        assertTrue(
            state.publications.all {
                it.status == PublicationStatus.VERIFIED || it.status == PublicationStatus.REJECTED
            }
        )
        assertEquals(state.approvedCount + state.rejectedCount, state.publications.size)
    }

    @Test
    fun `filtrar aprobadas y rechazadas actualiza la lista`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        viewModel.onFilterSelected(ModerationHistoryFilter.APPROVED)
        val approved = viewModel.uiState.value
        assertEquals(ModerationHistoryFilter.APPROVED, approved.filter)
        assertEquals(approved.approvedCount, approved.publications.size)
        assertTrue(approved.publications.all { it.status == PublicationStatus.VERIFIED })

        viewModel.onFilterSelected(ModerationHistoryFilter.REJECTED)
        val rejected = viewModel.uiState.value
        assertEquals(ModerationHistoryFilter.REJECTED, rejected.filter)
        assertEquals(rejected.rejectedCount, rejected.publications.size)
        assertTrue(rejected.publications.all { it.status == PublicationStatus.REJECTED })
    }

    @Test
    fun `el historial se actualiza al rechazar una publicacion pendiente`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val rejectedBefore = viewModel.uiState.value.rejectedCount

        RejectPublicationUseCase(repository)("murales-filandia", "Información inconsistente")

        val state = viewModel.uiState.value
        assertEquals(rejectedBefore + 1, state.rejectedCount)
        assertTrue(state.publications.any { it.id == "murales-filandia" })
    }
}
