package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.MAX_BIO_LENGTH
import com.app.rumbo.domain.model.ProfileField
import com.app.rumbo.domain.model.ProfileUpdate
import com.app.rumbo.testutil.FakeUserRepository
import com.app.rumbo.testutil.TestUsers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateProfileUseCaseTest {

    private val repository = FakeUserRepository()
    private val useCase = UpdateProfileUseCase(repository)

    @Test
    fun `datos validos se guardan en el usuario`() = runTest {
        val result = useCase(ProfileUpdate("Ana Gómez", "Hola", "Salento, Quindío"))

        val user = repository.observeCurrentUser().first()!!
        assertEquals(UpdateProfileResult.Success, result)
        assertEquals("Ana Gómez", user.name)
        assertEquals("Hola", user.bio)
        assertEquals("Salento, Quindío", user.city)
    }

    @Test
    fun `se guardan sin espacios sobrantes`() = runTest {
        useCase(ProfileUpdate("  Ana Gómez  ", "  Hola  ", "  Salento  "))

        val user = repository.observeCurrentUser().first()!!
        assertEquals("Ana Gómez", user.name)
        assertEquals("Hola", user.bio)
        assertEquals("Salento", user.city)
    }

    @Test
    fun `con datos invalidos devuelve los campos y no guarda nada`() = runTest {
        val result = useCase(ProfileUpdate("", "a".repeat(MAX_BIO_LENGTH + 1), "Salento"))

        assertEquals(UpdateProfileResult.Invalid(setOf(ProfileField.NAME, ProfileField.BIO)), result)
        assertEquals(TestUsers.mariana, repository.observeCurrentUser().first())
    }

    @Test
    fun `el correo y el nivel no cambian al editar`() = runTest {
        useCase(ProfileUpdate("Ana Gómez", "", "Salento"))

        val user = repository.observeCurrentUser().first()!!
        assertEquals(TestUsers.mariana.email, user.email)
        assertEquals(TestUsers.mariana.levelName, user.levelName)
    }
}
