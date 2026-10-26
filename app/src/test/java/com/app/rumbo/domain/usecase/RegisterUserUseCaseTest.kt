package com.app.rumbo.domain.usecase

import com.app.rumbo.data.repository.InMemoryUserRepository
import com.app.rumbo.domain.model.ProfileUpdate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RegisterUserUseCaseTest {

    @Test
    fun `registra un usuario con id propio, nombre y correo y lo selecciona como actual`() = runTest {
        val repository = InMemoryUserRepository()
        val registered = RegisterUserUseCase(repository)("  Ana Gómez  ", "  ana@example.com  ")

        assertNotEquals("me", registered.id)
        assertEquals("Ana Gómez", registered.name)
        assertEquals("ana@example.com", registered.email)
        assertEquals(registered, repository.observeCurrentUser().first())
    }

    @Test
    fun `cada registro crea un identificador distinto y reemplaza al usuario actual`() = runTest {
        val repository = InMemoryUserRepository()
        val register = RegisterUserUseCase(repository)

        val first = register("Ana Gómez", "ana@example.com")
        val second = register("Luis Pérez", "luis@example.com")

        assertNotEquals(first.id, second.id)
        assertEquals(second, repository.observeCurrentUser().first())
    }

    @Test
    fun `la edicion de perfil sigue actualizando al usuario registrado`() = runTest {
        val repository = InMemoryUserRepository()
        val registered = RegisterUserUseCase(repository)("Ana Gómez", "ana@example.com")

        val result = UpdateProfileUseCase(repository)(
            ProfileUpdate(name = "Ana G.", bio = "Me gustan los miradores", city = "Salento")
        )

        val updated = repository.observeCurrentUser().first()
        assertEquals(UpdateProfileResult.Success, result)
        assertNotNull(updated)
        assertEquals(registered.id, updated?.id)
        assertEquals("Ana G.", updated?.name)
        assertEquals("ana@example.com", updated?.email)
        assertEquals("Salento", updated?.city)
    }
}
