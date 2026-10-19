package com.app.rumbo.features.mypublications

import com.app.rumbo.data.local.CurrentUser
import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.Photo
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.PublicationDraft
import com.app.rumbo.domain.usecase.GetMyPublicationsUseCase
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
        val viewModel = MyPublicationsViewModel(GetMyPublicationsUseCase(repository))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.publications.collect() }

        assertTrue(viewModel.publications.value.isEmpty())
    }

    @Test
    fun `incluye solo las publicaciones del usuario actual`() = runTest {
        PublishPublicationUseCase(repository).invoke(draft("Mirador de la Vereda Cruces"), CurrentUser.author)
        val viewModel = MyPublicationsViewModel(GetMyPublicationsUseCase(repository))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.publications.collect() }

        val mine = viewModel.publications.value
        assertEquals(1, mine.size)
        assertEquals("Mirador de la Vereda Cruces", mine.first().title)
        assertTrue(mine.all { it.author.id == CurrentUser.author.id })
    }
}
