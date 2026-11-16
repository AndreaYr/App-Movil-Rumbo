package com.app.rumbo.domain.usecase

import com.app.rumbo.R
import com.app.rumbo.data.repository.InMemoryUserRepository
import com.app.rumbo.domain.exception.InvalidRoleException
import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.model.UserRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RoleBasedLoginTest {

    private val repository = InMemoryUserRepository()

    private suspend fun register(role: UserRole, email: String = "cuenta@rumbo.co"): User {
        val saved = repository.createUser(
            User(id = "", name = "Cuenta", email = email, city = "", levelName = "Explorador", password = "Clave12345", role = role)
        )
        repository.logout()
        return saved
    }

    @Test
    fun `un usuario normal entra por el acceso de usuarios`() = runTest {
        register(UserRole.USER)

        val user = LoginUserUseCase(repository)("cuenta@rumbo.co", "Clave12345")

        assertEquals(UserRole.USER, user?.role)
    }

    @Test
    fun `un moderador no puede entrar por el acceso de usuarios y no queda con sesion`() = runTest {
        register(UserRole.MODERATOR)

        try {
            LoginUserUseCase(repository)("cuenta@rumbo.co", "Clave12345")
            fail("Debía rechazar al moderador")
        } catch (_: InvalidRoleException) {
        }

        assertNull(repository.observeCurrentUser().first())
    }

    @Test
    fun `una clave incorrecta sigue siendo credenciales invalidas y no un error de rol`() = runTest {
        register(UserRole.MODERATOR)

        assertNull(LoginUserUseCase(repository)("cuenta@rumbo.co", "otra-clave"))
    }

    @Test
    fun `un moderador no puede entrar con Google por el acceso de usuarios`() = runTest {
        register(UserRole.MODERATOR, email = "mod@google.test")

        try {
            SignInWithGoogleUseCase(repository)("mod")
            fail("Debía rechazar al moderador")
        } catch (_: InvalidRoleException) {
        }

        assertNull(repository.observeCurrentUser().first())
    }

    @Test
    fun `un moderador registrado entra con Google por el acceso de moderador`() = runTest {
        register(UserRole.MODERATOR, email = "mod@google.test")

        val result = SignInModeratorWithGoogleUseCase(repository)("mod")

        assertTrue(result.isSuccess)
        assertEquals(UserRole.MODERATOR, repository.observeCurrentUser().first()?.role)
    }

    @Test
    fun `un usuario normal no entra con Google por el acceso de moderador`() = runTest {
        register(UserRole.USER, email = "ana@google.test")

        val result = SignInModeratorWithGoogleUseCase(repository)("ana")

        assertEquals(
            R.string.auth_error_invalid_credentials_or_role,
            (result.exceptionOrNull() as LoginModeratorException).messageRes
        )
        assertNull(repository.observeCurrentUser().first())
    }

    @Test
    fun `una cuenta de Google sin registrar no se crea desde el acceso de moderador`() = runTest {
        val result = SignInModeratorWithGoogleUseCase(repository)("desconocido")

        assertTrue(result.isFailure)
        assertNull(repository.observeCurrentUser().first())
    }

    @Test
    fun `un usuario normal con clave correcta recibe el mensaje de rol invalido en el acceso de moderador`() = runTest {
        register(UserRole.USER)

        val result = LoginModeratorUseCase(repository)("cuenta@rumbo.co", "Clave12345")

        assertEquals(
            R.string.auth_error_invalid_credentials_or_role,
            (result.exceptionOrNull() as LoginModeratorException).messageRes
        )
        assertNull(repository.observeCurrentUser().first())
    }
}
