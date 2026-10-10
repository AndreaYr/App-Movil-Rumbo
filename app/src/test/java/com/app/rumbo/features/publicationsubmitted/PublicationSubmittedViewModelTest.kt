package com.app.rumbo.features.publicationsubmitted

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Test

class PublicationSubmittedViewModelTest {

    @Test
    fun `muestra el titulo de la publicacion enviada`() {
        val viewModel = PublicationSubmittedViewModel(
            SavedStateHandle(mapOf(PublicationSubmittedDestination.ARG_TITLE to "Mirador de la Vereda Cruces"))
        )

        assertEquals("Mirador de la Vereda Cruces", viewModel.title)
    }
}
