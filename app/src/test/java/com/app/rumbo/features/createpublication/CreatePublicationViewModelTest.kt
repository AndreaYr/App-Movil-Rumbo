package com.app.rumbo.features.createpublication

import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.PublicationStatus
import com.app.rumbo.domain.usecase.PublishPublicationUseCase
import com.app.rumbo.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreatePublicationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = InMemoryPublicationRepository()

    private fun createViewModel() = CreatePublicationViewModel(PublishPublicationUseCase(repository))

    @Test
    fun `el formulario inicia con los valores del prototipo`() {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertEquals(Category.NATURE, state.category)
        assertEquals(PriceLevel.CHEAP, state.price)
        assertEquals("7:00 - 18:00", state.schedule)
        assertTrue(state.photos.isEmpty())
        assertTrue(state.canAddPhoto)
    }

    @Test
    fun `la descripcion se recorta al maximo de 500 caracteres`() {
        val viewModel = createViewModel()

        viewModel.onDescriptionChange("a".repeat(620))

        assertEquals(MAX_DESCRIPTION_LENGTH, viewModel.uiState.value.description.length)
    }

    @Test
    fun `no se agregan mas de seis fotos`() {
        val viewModel = createViewModel()

        viewModel.onPhotosAdded(List(10) { "content://foto/$it" })

        assertEquals(MAX_PHOTOS, viewModel.uiState.value.photos.size)
        assertFalse(viewModel.uiState.value.canAddPhoto)
    }

    @Test
    fun `quitar una foto la elimina de la lista`() {
        val viewModel = createViewModel()
        viewModel.onPhotosAdded(listOf("content://foto/1", "content://foto/2"))

        viewModel.onRemovePhoto(0)

        assertEquals(1, viewModel.uiState.value.photos.size)
        assertEquals("content://foto/2", viewModel.uiState.value.photos.first().url)
    }

    @Test
    fun `no publica si faltan campos obligatorios`() = runTest {
        val viewModel = createViewModel()
        val before = repository.observePublications().first().size

        viewModel.onPublish()

        assertTrue(viewModel.uiState.value.showValidationErrors)
        assertEquals(before, repository.observePublications().first().size)
    }

    @Test
    fun `publicar agrega una publicacion pendiente y visible en el feed`() = runTest {
        val viewModel = createViewModel()
        viewModel.onPhotosAdded(listOf("content://foto/1"))
        viewModel.onTitleChange("Mirador de la Vereda Cruces")
        viewModel.onDescriptionChange("Mirador poco conocido sobre el cañón")
        viewModel.onCategorySelected(Category.NATURE)

        viewModel.onPublish()

        val created = repository.observePublications().first()
            .first { it.title == "Mirador de la Vereda Cruces" }
        assertEquals(PublicationStatus.PENDING, created.status)
        assertEquals(Category.NATURE, created.category)
        assertTrue(created.photos.isNotEmpty())
    }

    @Test
    fun `se puede publicar sin fotos`() = runTest {
        val viewModel = createViewModel()
        viewModel.onTitleChange("Café de la Plaza")
        viewModel.onDescriptionChange("Cafetería en la plaza principal")

        viewModel.onPublish()

        val created = repository.observePublications().first().first { it.title == "Café de la Plaza" }
        assertEquals(PublicationStatus.PENDING, created.status)
        assertTrue(created.photos.isEmpty())
    }

    @Test
    fun `mover el marcador actualiza las coordenadas`() {
        val viewModel = createViewModel()

        viewModel.onMarkerMoved(latitude = 4.6, longitude = -75.5)

        assertEquals(4.6, viewModel.uiState.value.latitude, 0.0001)
        assertEquals(-75.5, viewModel.uiState.value.longitude, 0.0001)
    }
}
