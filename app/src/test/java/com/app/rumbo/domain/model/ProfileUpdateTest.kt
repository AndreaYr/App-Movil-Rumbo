package com.app.rumbo.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileUpdateTest {

    private fun valid() = ProfileUpdate(name = "Mariana Ospina", bio = "Me gustan los miradores.", city = "Armenia, Quindío")

    @Test
    fun `datos correctos no tienen errores`() {
        assertTrue(valid().isValid)
        assertTrue(valid().errors.isEmpty())
    }

    @Test
    fun `el nombre en blanco es invalido`() {
        assertEquals(setOf(ProfileField.NAME), valid().copy(name = "   ").errors)
    }

    @Test
    fun `el nombre de mas de 60 caracteres es invalido`() {
        assertTrue(valid().copy(name = "a".repeat(MAX_NAME_LENGTH)).isValid)
        assertEquals(setOf(ProfileField.NAME), valid().copy(name = "a".repeat(MAX_NAME_LENGTH + 1)).errors)
    }

    @Test
    fun `la biografia es opcional pero no pasa de 200 caracteres`() {
        assertTrue(valid().copy(bio = "").isValid)
        assertTrue(valid().copy(bio = "a".repeat(MAX_BIO_LENGTH)).isValid)
        assertEquals(setOf(ProfileField.BIO), valid().copy(bio = "a".repeat(MAX_BIO_LENGTH + 1)).errors)
    }

    @Test
    fun `la ciudad es obligatoria`() {
        assertEquals(setOf(ProfileField.CITY), valid().copy(city = "").errors)
    }

    @Test
    fun `los espacios de los extremos no cuentan para el limite`() {
        assertTrue(valid().copy(name = "  " + "a".repeat(MAX_NAME_LENGTH) + "  ").isValid)
    }

    @Test
    fun `varios campos invalidos se reportan juntos`() {
        val update = ProfileUpdate(name = "", bio = "a".repeat(MAX_BIO_LENGTH + 1), city = "")

        assertEquals(setOf(ProfileField.NAME, ProfileField.BIO, ProfileField.CITY), update.errors)
    }
}
