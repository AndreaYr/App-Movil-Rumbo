package com.app.rumbo.features.register

import com.app.rumbo.data.repository.InMemoryUserRepository
import com.app.rumbo.domain.usecase.RegisterUserUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterViewModelTest {

    @Test
    fun `al enviar formulario invalido muestra los errores en el estado`() {
        val viewModel = RegisterViewModel(RegisterUserUseCase(InMemoryUserRepository()))

        viewModel.register()

        val state = viewModel.uiState.value
        assertTrue(state.hasSubmitted)
        assertEquals("El nombre es obligatorio.", state.nameError)
        assertEquals("El correo electrónico es obligatorio.", state.emailError)
        assertEquals("La contraseña es obligatoria.", state.passwordError)
        assertEquals("Confirma tu contraseña.", state.confirmPasswordError)
        assertEquals(RegisterStatus.IDLE, state.status)
    }
}
