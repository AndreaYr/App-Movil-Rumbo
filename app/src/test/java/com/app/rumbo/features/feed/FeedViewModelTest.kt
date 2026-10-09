package com.app.rumbo.features.feed

import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.FeedSort
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.usecase.GetFeedUseCase
import com.app.rumbo.domain.usecase.ToggleInterestUseCase
import com.app.rumbo.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: FeedViewModel

    @Before
    fun setUp() {
        val repository = InMemoryPublicationRepository()
        viewModel = FeedViewModel(GetFeedUseCase(repository), ToggleInterestUseCase(repository))
    }

    @Test
    fun `el feed carga y oculta las publicaciones rechazadas`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.publications.isNotEmpty())
        assertTrue(state.publications.none { it.status == PublicationStatus.REJECTED })
    }

    @Test
    fun `filtrar por categoria muestra solo esa categoria y tocarla de nuevo la quita`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        viewModel.onCategorySelected(Category.CULTURE)
        assertEquals(Category.CULTURE, viewModel.uiState.value.selectedCategory)
        assertTrue(viewModel.uiState.value.publications.all { it.category == Category.CULTURE })

        viewModel.onCategorySelected(Category.CULTURE)
        assertNull(viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `ordenar por mas votadas deja primero la de mas votos`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        viewModel.onSortSelected(FeedSort.MOST_VOTED)

        val votes = viewModel.uiState.value.publications.map { it.votes }
        assertEquals(votes.sortedDescending(), votes)
    }

    @Test
    fun `me interesa suma un voto y volver a tocarlo lo resta`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val before = viewModel.uiState.value.publications.first { it.id == "valle-cocora" }

        viewModel.onToggleInterest("valle-cocora")
        val after = viewModel.uiState.value.publications.first { it.id == "valle-cocora" }
        assertTrue(after.interested)
        assertEquals(before.votes + 1, after.votes)

        viewModel.onToggleInterest("valle-cocora")
        assertEquals(before.votes, viewModel.uiState.value.publications.first { it.id == "valle-cocora" }.votes)
    }
}
