package com.app.rumbo.features.publicationdetail

import androidx.lifecycle.SavedStateHandle
import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.usecase.GetPublicationDetailUseCase
import com.app.rumbo.domain.usecase.ToggleInterestUseCase
import com.app.rumbo.domain.usecase.ToggleVisitedUseCase
import com.app.rumbo.navigation.PublicationDetailDestination
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
class PublicationDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModelFor(id: String): PublicationDetailViewModel {
        val repository = InMemoryPublicationRepository()
        return PublicationDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf(PublicationDetailDestination.ARG_ID to id)),
            getPublicationDetail = GetPublicationDetailUseCase(repository),
            toggleInterest = ToggleInterestUseCase(repository),
            toggleVisited = ToggleVisitedUseCase(repository)
        )
    }

    @Test
    fun `carga el detalle de la publicacion indicada`() = runTest {
        val viewModel = viewModelFor("valle-cocora")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        val state = viewModel.uiState.value
        assertTrue(state is PublicationDetailUiState.Success)
        assertEquals("Valle de Cocora", (state as PublicationDetailUiState.Success).publication.title)
    }

    @Test
    fun `un id inexistente muestra no encontrada`() = runTest {
        val viewModel = viewModelFor("no-existe")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        assertEquals(PublicationDetailUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun `marcar visitado cambia el estado de la publicacion`() = runTest {
        val viewModel = viewModelFor("museo-oro-quimbaya")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val before = (viewModel.uiState.value as PublicationDetailUiState.Success).publication.visited

        viewModel.onToggleVisited()

        val after = (viewModel.uiState.value as PublicationDetailUiState.Success).publication.visited
        assertEquals(!before, after)
    }
}
