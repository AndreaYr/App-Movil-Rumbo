package com.app.rumbo.features.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LoginStatus { IDLE, LOADING, SUCCESS, ERROR }

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val status: LoginStatus = LoginStatus.IDLE,
    val message: String? = null
) {
    val isFormValid: Boolean
        get() = emailError == null && passwordError == null &&
            Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= MIN_PASSWORD_LENGTH

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { state ->
            state.copy(
                email = email,
                emailError = if (email.isBlank()) null else validateEmail(email),
                status = if (state.status == LoginStatus.LOADING) LoginStatus.LOADING else LoginStatus.IDLE,
                message = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { state ->
            state.copy(
                password = password,
                passwordError = if (password.isEmpty()) null else validatePassword(password),
                status = if (state.status == LoginStatus.LOADING) LoginStatus.LOADING else LoginStatus.IDLE,
                message = null
            )
        }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        val current = _uiState.value
        val emailError = validateEmail(current.email)
        val passwordError = validatePassword(current.password)
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        if (current.status == LoginStatus.LOADING) return
        val submittedEmail = current.email.trim()
        val submittedPassword = current.password

        viewModelScope.launch {
            _uiState.update { it.copy(status = LoginStatus.LOADING, message = null) }
            delay(SIMULATED_REQUEST_DELAY_MS)
            val successful = submittedEmail.equals(TEST_EMAIL, ignoreCase = true) &&
                submittedPassword == TEST_PASSWORD
            _uiState.update {
                it.copy(
                    status = if (successful) LoginStatus.SUCCESS else LoginStatus.ERROR,
                    message = if (successful) "Inicio de sesión exitoso." else "Correo o contraseña incorrectos."
                )
            }
        }
    }

    fun resetResult() {
        _uiState.update { it.copy(status = LoginStatus.IDLE, message = null) }
    }

    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> "El correo electrónico es obligatorio."
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Ingresa un correo electrónico válido."
        else -> null
    }

    private fun validatePassword(password: String): String? = when {
        password.isBlank() -> "La contraseña es obligatoria."
        password.length < MIN_PASSWORD_LENGTH -> "La contraseña debe tener al menos 6 caracteres."
        else -> null
    }

    private companion object {
        const val TEST_EMAIL = "prueba@rumbo.app"
        const val TEST_PASSWORD = "rumbo123"
        const val MIN_PASSWORD_LENGTH = 6
        const val SIMULATED_REQUEST_DELAY_MS = 800L
    }
}
