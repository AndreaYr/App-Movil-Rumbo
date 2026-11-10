package com.app.rumbo.domain.usecase

import com.app.rumbo.data.local.CurrentUser
import com.app.rumbo.data.repository.InMemoryPublicationRepository
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.PublicationDraft
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PublishDepartmentTest {

    @Test
    fun `la publicacion nueva conserva el departamento y el municipio del borrador`() = runTest {
        val draft = PublicationDraft(
            title = "Mirador", description = "Vista", category = Category.NATURE, schedule = "",
            price = PriceLevel.FREE, latitude = 4.5, longitude = -75.6, photos = emptyList(),
            department = "Quindío", town = "Salento"
        )

        val publication = PublishPublicationUseCase(InMemoryPublicationRepository())(draft, CurrentUser.author)

        assertEquals("Quindío", publication.department)
        assertEquals("Salento", publication.town)
    }
}
