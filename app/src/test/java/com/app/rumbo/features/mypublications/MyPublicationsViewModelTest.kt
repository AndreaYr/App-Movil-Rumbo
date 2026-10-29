package com.app.rumbo.features.mypublications

import com.app.rumbo.data.local.CurrentUser
import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.Photo
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.PublicationDraft
import com.app.rumbo.domain.usecase.FinishPublicationUseCase
import com.app.rumbo.domain.usecase.GetMyPublicationsUseCase
import com.app.rumbo.domain.usecase.RejectPublicationUseCase
import com.app.rumbo.domain.usecase.VerifyPublicationUseCase
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.usecase.PublishPublicationUseCase
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
class MyPublicationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = InMemoryPublicationRepository()

    private fun draft(title: String) = PublicationDraft(
        title = title,
        description = "Descripción",
        category = Category.NATURE,
        schedule = "7:00 - 18:00",
        price = PriceLevel.CHEAP,
        latitude = 4.5871,
        longitude = -75.6429,
        photos = listOf(Photo(url = "content://foto/1", description = "Foto"))
    )

    @Test
    fun `sin publicaciones propias la lista esta vacia`() = runTest {
        val viewModel = MyPublicationsViewModel(GetMyPublicationsUseCase(repository), FinishPublicationUseCase(repository))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        assertTrue(viewModel.uiState.value.publications.isEmpty())
    }

    @Test
    fun `incluye solo las publicaciones del usuario actual`() = runTest {
        PublishPublicationUseCase(repository).invoke(draft("Mirador de la Vereda Cruces"), CurrentUser.author)
        val viewModel = MyPublicationsViewModel(GetMyPublicationsUseCase(repository), FinishPublicationUseCase(repository))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        val mine = viewModel.uiState.value.publications
        assertEquals(1, mine.size)
        assertEquals("Mirador de la Vereda Cruces", mine.first().title)
        assertTrue(mine.all { it.author.id == CurrentUser.author.id })
    }

    @Test
    fun `la pestana filtra por estado y cuenta cada grupo`() = runTest {
        val publish = PublishPublicationUseCase(repository)
        val approved = publish(draft("Aprobada"), CurrentUser.author)
        val rejected = publish(draft("Rechazada"), CurrentUser.author)
        publish(draft("Pendiente"), CurrentUser.author)
        VerifyPublicationUseCase(repository)(approved.id)
        RejectPublicationUseCase(repository)(rejected.id, "Ubicación incorrecta", "El marcador quedó sobre la vía")
        val viewModel = MyPublicationsViewModel(GetMyPublicationsUseCase(repository), FinishPublicationUseCase(repository))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        viewModel.onTabSelected(MyPublicationsTab.REJECTED)

        val state = viewModel.uiState.value
        assertEquals(listOf("Rechazada"), state.visiblePublications.map { it.title })
        assertEquals("Ubicación incorrecta", state.visiblePublications.single().rejectionReason)
        assertEquals(3, state.countFor(MyPublicationsTab.ALL))
        assertEquals(1, state.countFor(MyPublicationsTab.VERIFIED))
        assertEquals(1, state.countFor(MyPublicationsTab.PENDING))
    }

    @Test
    fun `finalizar pide confirmacion y cierra la publicacion verificada`() = runTest {
        val published = PublishPublicationUseCase(repository).invoke(draft("Sendero"), CurrentUser.author)
        VerifyPublicationUseCase(repository)(published.id)
        val viewModel = MyPublicationsViewModel(GetMyPublicationsUseCase(repository), FinishPublicationUseCase(repository))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        viewModel.onFinishRequested(published.id)
        assertEquals(published.id, viewModel.uiState.value.publicationToFinish?.id)

        viewModel.onFinishConfirmed()

        assertEquals(null, viewModel.uiState.value.publicationToFinish)
        assertEquals(PublicationStatus.FINISHED, viewModel.uiState.value.publications.single().status)
    }

    @Test
    fun `cancelar la confirmacion no cambia el estado`() = runTest {
        val published = PublishPublicationUseCase(repository).invoke(draft("Sendero"), CurrentUser.author)
        VerifyPublicationUseCase(repository)(published.id)
        val viewModel = MyPublicationsViewModel(GetMyPublicationsUseCase(repository), FinishPublicationUseCase(repository))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        viewModel.onFinishRequested(published.id)
        viewModel.onFinishDismissed()

        assertEquals(null, viewModel.uiState.value.publicationToFinish)
        assertEquals(PublicationStatus.VERIFIED, viewModel.uiState.value.publications.single().status)
    }
}
