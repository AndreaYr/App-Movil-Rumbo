package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.PlaceResult
import com.app.rumbo.testutil.FakePlaceSearchRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchPlacesUseCaseTest {

    private val place = PlaceResult("1", "Parque de la Cultura", "Calarcá, Quindío", 4.53, -75.64)

    @Test
    fun `una consulta muy corta no llama al servicio`() = runTest {
        val repository = FakePlaceSearchRepository(listOf(place))

        val result = SearchPlacesUseCase(repository)("  ab ", 4.5, -75.6)

        assertTrue(result.isEmpty())
        assertTrue(repository.queries.isEmpty())
    }

    @Test
    fun `la consulta se envia sin espacios sobrantes y devuelve los lugares`() = runTest {
        val repository = FakePlaceSearchRepository(listOf(place))

        val result = SearchPlacesUseCase(repository)("  parque cultura ", 4.5, -75.6)

        assertEquals(listOf(place), result)
        assertEquals(listOf("parque cultura"), repository.queries)
    }
}
