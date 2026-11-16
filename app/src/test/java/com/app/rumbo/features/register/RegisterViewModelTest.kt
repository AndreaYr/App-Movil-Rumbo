package com.app.rumbo.features.register

import com.app.rumbo.R
import com.app.rumbo.data.repository.InMemoryUserRepository
import com.app.rumbo.domain.usecase.RegisterUserUseCase
import com.app.rumbo.domain.usecase.SignInWithGoogleUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterViewModelTest {

    @Test
    fun `al enviar formulario invalido muestra los errores en el estado`() {
        val repository = InMemoryUserRepository()
        val viewModel = RegisterViewModel(RegisterUserUseCase(repository), SignInWithGoogleUseCase(repository))

        viewModel.register()

        val state = viewModel.uiState.value
        assertTrue(state.hasSubmitted)
        assertEquals(R.string.validation_name_required, state.nameError)
        assertEquals(R.string.validation_email_required, state.emailError)
        assertEquals(R.string.validation_password_required, state.passwordError)
        assertEquals(R.string.validation_confirm_password_required, state.confirmPasswordError)
        assertEquals(RegisterStatus.IDLE, state.status)
    }
}
