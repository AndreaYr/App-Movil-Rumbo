package com.app.rumbo.features.loginmoderator

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.usecase.LoginModeratorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LoginModeratorStatus { IDLE, LOADING, SUCCESS, ERROR }

data class LoginModeratorUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val status: LoginModeratorStatus = LoginModeratorStatus.IDLE,
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
class LoginModeratorViewModel @Inject constructor(
    private val loginModeratorUseCase: LoginModeratorUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginModeratorUiState())
    val uiState: StateFlow<LoginModeratorUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { state ->
            state.copy(
                email = email,
                emailError = if (email.isBlank()) null else validateEmail(email),
                status = if (state.status == LoginModeratorStatus.LOADING) LoginModeratorStatus.LOADING else LoginModeratorStatus.IDLE,
                message = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { state ->
            state.copy(
                password = password,
                passwordError = if (password.isEmpty()) null else validatePassword(password),
                status = if (state.status == LoginModeratorStatus.LOADING) LoginModeratorStatus.LOADING else LoginModeratorStatus.IDLE,
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
        if (current.status == LoginModeratorStatus.LOADING) return
        val submittedEmail = current.email
        val submittedPassword = current.password

        viewModelScope.launch {
            _uiState.update { it.copy(status = LoginModeratorStatus.LOADING, message = null) }
            loginModeratorUseCase(submittedEmail, submittedPassword)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            status = LoginModeratorStatus.SUCCESS,
                            message = "Acceso concedido."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            status = LoginModeratorStatus.ERROR,
                            message = error.message ?: "Error al iniciar sesión."
                        )
                    }
                }
        }
    }

    fun resetResult() {
        _uiState.update { it.copy(status = LoginModeratorStatus.IDLE, message = null) }
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
        const val MIN_PASSWORD_LENGTH = 6
    }
}
