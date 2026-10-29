package com.app.rumbo.features.publicationdetail

import androidx.lifecycle.SavedStateHandle
import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.usecase.AddCommentUseCase
import com.app.rumbo.domain.usecase.GetPublicationDetailUseCase
import com.app.rumbo.domain.usecase.ToggleCommentLikeUseCase
import com.app.rumbo.domain.usecase.ToggleInterestUseCase
import com.app.rumbo.domain.usecase.ToggleVisitedUseCase
import com.app.rumbo.navigation.PublicationDetailDestination
import com.app.rumbo.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.app.rumbo.domain.model.Comment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
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
            toggleVisited = ToggleVisitedUseCase(repository),
            addComment = AddCommentUseCase(repository),
            toggleCommentLike = ToggleCommentLikeUseCase(repository)
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

    @Test
    fun `enviar un comentario lo agrega al inicio y limpia el campo`() = runTest {
        val viewModel = viewModelFor("valle-cocora")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val before = (viewModel.uiState.value as PublicationDetailUiState.Success).publication.comments.size

        viewModel.onCommentChange("  Muy recomendado para ir en familia  ")
        viewModel.onSendComment()

        val comments = (viewModel.uiState.value as PublicationDetailUiState.Success).publication.comments
        assertEquals(before + 1, comments.size)
        assertEquals("Muy recomendado para ir en familia", comments.first().text)
        assertEquals("", viewModel.commentText.value)
    }

    @Test
    fun `un comentario vacio no se envia`() = runTest {
        val viewModel = viewModelFor("valle-cocora")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val before = (viewModel.uiState.value as PublicationDetailUiState.Success).publication.comments.size

        viewModel.onCommentChange("   ")
        viewModel.onSendComment()

        val after = (viewModel.uiState.value as PublicationDetailUiState.Success).publication.comments.size
        assertEquals(before, after)
    }

    @Test
    fun `una publicacion pendiente no recibe comentarios y se conserva el texto`() = runTest {
        val viewModel = viewModelFor("murales-filandia")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        val before = (viewModel.uiState.value as PublicationDetailUiState.Success).publication.comments.size

        viewModel.onCommentChange("Hola")
        viewModel.onSendComment()

        val after = (viewModel.uiState.value as PublicationDetailUiState.Success).publication.comments.size
        assertEquals(before, after)
        assertEquals("Hola", viewModel.commentText.value)
    }

    @Test
    fun `el texto del comentario se limita a 300 caracteres`() = runTest {
        val viewModel = viewModelFor("valle-cocora")

        viewModel.onCommentChange("a".repeat(400))

        assertEquals(300, viewModel.commentText.value.length)
    }

    @Test
    fun `el me gusta de un comentario suma y al repetirlo resta`() = runTest {
        val viewModel = viewModelFor("valle-cocora")
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        fun first() = (viewModel.uiState.value as PublicationDetailUiState.Success).publication.comments.first()
        val original = first()

        viewModel.onToggleCommentLike(original.id)
        assertEquals(original.likes + 1, first().likes)
        assertTrue(first().liked)

        viewModel.onToggleCommentLike(original.id)
        assertEquals(original.likes, first().likes)
        assertEquals(false, first().liked)
    }

    @Test
    fun `no se da me gusta en publicaciones que no estan verificadas`() = runTest {
        val repository = InMemoryPublicationRepository()
        val pending = repository.observePublication("murales-filandia").first()!!
        val comment = Comment("c1", pending.author, "Texto", 0L, 2)
        repository.addComment(pending.id, comment)

        assertEquals(false, ToggleCommentLikeUseCase(repository)(pending.id, comment.id))
        assertEquals(2, repository.observePublication(pending.id).first()!!.comments.first().likes)
    }
}
