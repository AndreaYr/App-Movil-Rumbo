package com.app.rumbo.features.feed

import com.app.rumbo.data.local.SamplePublications
import com.app.rumbo.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapFiltersTest {

    private val publications = SamplePublications.create()

    @Test
    fun `sin criterios el filtro no esta activo`() {
        assertFalse(MapFilters().isActive)
        assertEquals(0, MapFilters().count)
    }

    @Test
    fun `filtra por varias categorias`() {
        val filters = MapFilters(categories = setOf(Category.CULTURE, Category.NATURE))

        val result = publications.filter(filters::matches)

        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.category == Category.CULTURE || it.category == Category.NATURE })
    }

    @Test
    fun `filtra por ciudad ignorando mayusculas`() {
        val town = publications.first().town
        val result = publications.filter(MapFilters(town = town.uppercase())::matches)

        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.town == town })
    }

    @Test
    fun `me interesa y visitado se combinan con las demas condiciones`() {
        val filters = MapFilters(interested = true, visited = true)

        assertTrue(publications.filter(filters::matches).all { it.interested && it.visited })
        assertEquals(2, filters.count)
    }

    @Test
    fun `el departamento filtra por los municipios que le pertenecen`() {
        val quindio = setOf("armenia", "salento", "filandia", "montenegro", "circasia").map(::normalizeName).toSet()
        val inQuindio = publications.filter { MapFilters(department = "Quindío").matches(it, quindio) }
        val inChoco = publications.filter { MapFilters(department = "Chocó").matches(it, setOf("quibdo")) }

        assertTrue(inQuindio.isNotEmpty())
        assertTrue(inQuindio.all { normalizeName(it.town) in quindio })
        assertTrue(inChoco.isEmpty())
    }

    @Test
    fun `si la publicacion trae departamento se compara directo sin depender del municipio`() {
        val salento = publications.first { it.town == "Salento" }
        val withDepartment = salento.copy(department = "Quindío")
        // Aunque la lista de municipios no lo incluya, manda el departamento de la publicación
        assertTrue(MapFilters(department = "quindio").matches(withDepartment, emptySet()))
        assertFalse(MapFilters(department = "Chocó").matches(withDepartment, setOf("salento")))
    }

    @Test
    fun `normalizar quita tildes y mayusculas`() {
        assertEquals("calarca", normalizeName(" CALARCÁ "))
        assertEquals("bogota, d.c.", normalizeName("Bogotá, D.C."))
    }
}
