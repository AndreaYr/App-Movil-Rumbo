package com.app.rumbo.features.login

import com.app.rumbo.R
import com.app.rumbo.data.repository.InMemoryUserRepository
import com.app.rumbo.domain.usecase.LoginUserUseCase
import com.app.rumbo.domain.usecase.SignInWithGoogleUseCase
import com.app.rumbo.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(): LoginViewModel {
        val repository = InMemoryUserRepository()
        return LoginViewModel(LoginUserUseCase(repository), SignInWithGoogleUseCase(repository))
    }

    @Test
    fun `con un token de Google valido el login termina en exito`() {
        val viewModel = viewModel()

        viewModel.loginWithGoogle("token")

        assertEquals(LoginStatus.SUCCESS, viewModel.uiState.value.status)
    }

    @Test
    fun `si Google falla se muestra el mensaje de error de Google`() {
        val viewModel = viewModel()

        viewModel.onGoogleError()

        val state = viewModel.uiState.value
        assertEquals(LoginStatus.ERROR, state.status)
        assertEquals(R.string.google_sign_in_error, state.messageRes)
    }
}
