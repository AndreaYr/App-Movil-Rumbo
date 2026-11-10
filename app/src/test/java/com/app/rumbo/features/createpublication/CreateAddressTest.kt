package com.app.rumbo.features.createpublication

import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.model.PlaceResult
import com.app.rumbo.domain.usecase.PublishPublicationUseCase
import com.app.rumbo.domain.usecase.ReverseGeocodeUseCase
import com.app.rumbo.domain.usecase.SearchPlacesUseCase
import com.app.rumbo.testutil.FakePlaceSearchRepository
import com.app.rumbo.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateAddressTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val place = PlaceResult("1", "Calle 40 #25-10", "Calarcá, Quindío", 4.52, -75.64)

    private fun createViewModel(places: FakePlaceSearchRepository) = CreatePublicationViewModel(
        PublishPublicationUseCase(InMemoryPublicationRepository()),
        SearchPlacesUseCase(places),
        ReverseGeocodeUseCase(places)
    )

    @Test
    fun `al mover el marcador se detecta la direccion de ese punto`() = runTest {
        val viewModel = createViewModel(FakePlaceSearchRepository(reverseResult = place))

        viewModel.onMarkerMoved(4.52, -75.64)
        advanceUntilIdle()

        assertEquals("Calle 40 #25-10, Calarcá, Quindío", viewModel.uiState.value.address)
        assertEquals(4.52, viewModel.uiState.value.latitude, 0.0)
    }

    @Test
    fun `elegir una sugerencia coloca el marcador en su punto y mueve la camara`() = runTest {
        val viewModel = createViewModel(FakePlaceSearchRepository())

        viewModel.onAddressSuggestionSelected(place)

        val state = viewModel.uiState.value
        assertEquals(place.fullAddress, state.address)
        assertEquals(4.52, state.latitude, 0.0)
        assertEquals(-75.64, state.longitude, 0.0)
        assertNotNull(state.focusLatitude)
        assertTrue(state.addressSuggestions.isEmpty())
    }

    @Test
    fun `al escribir la direccion se buscan sugerencias despues de una pausa`() = runTest {
        val places = FakePlaceSearchRepository(results = listOf(place))
        val viewModel = createViewModel(places)

        viewModel.onAddressChange("calle 40")
        advanceUntilIdle()

        assertEquals(listOf(place), viewModel.uiState.value.addressSuggestions)
        assertEquals(listOf("calle 40"), places.queries)
    }
}
