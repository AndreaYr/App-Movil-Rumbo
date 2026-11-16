package com.app.rumbo.features.moderatorfinish

import com.app.rumbo.data.local.SamplePublications
import com.app.rumbo.domain.model.PublicationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModeratorFinishUiStateTest {

    private val verified = SamplePublications.create().filter { it.status == PublicationStatus.VERIFIED }

    @Test
    fun `sin eleccion se usa la primera publicacion`() {
        val state = ModeratorFinishUiState(isLoading = false, publications = verified)

        assertEquals(verified.first(), state.publication)
    }

    @Test
    fun `se usa la publicacion que eligio el moderador`() {
        val chosen = verified.last()

        val state = ModeratorFinishUiState(isLoading = false, publications = verified, selectedId = chosen.id)

        assertEquals(chosen, state.publication)
    }

    @Test
    fun `si la elegida ya no esta disponible vuelve a la primera`() {
        val state = ModeratorFinishUiState(isLoading = false, publications = verified, selectedId = "ya-no-existe")

        assertEquals(verified.first(), state.publication)
    }

    @Test
    fun `sin publicaciones no hay nada que cerrar`() {
        assertNull(ModeratorFinishUiState(isLoading = false).publication)
    }
}
