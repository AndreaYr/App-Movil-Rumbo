package com.app.rumbo.features.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.usecase.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RegisterStatus { IDLE, LOADING, SUCCESS, ERROR }

private fun isValidEmail(email: String) =
    Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val acceptedTerms: Boolean = false,
    val termsError: String? = null,
    val hasSubmitted: Boolean = false,
    val status: RegisterStatus = RegisterStatus.IDLE
) {
    val hasMinimumLength: Boolean get() = password.length >= MIN_PASSWORD_LENGTH
    val hasUppercase: Boolean get() = password.any(Char::isUpperCase)
    val hasNumber: Boolean get() = password.any(Char::isDigit)
    val passwordRuleCount: Int
        get() = listOf(hasMinimumLength, hasUppercase, hasNumber).count { it }
    private companion object {
        const val MIN_PASSWORD_LENGTH = 10
    }
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUser: RegisterUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { state ->
            state.copy(
                name = name,
                nameError = if (state.hasSubmitted || name.isNotBlank()) validateName(name) else null,
                status = state.nextStatus()
            )
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { state ->
            state.copy(
                email = email,
                emailError = if (state.hasSubmitted || email.isNotBlank()) validateEmail(email) else null,
                status = state.nextStatus()
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { state ->
            state.copy(
                password = password,
                passwordError = if (state.hasSubmitted || password.isNotEmpty()) validatePassword(password) else null,
                confirmPasswordError = if (state.hasSubmitted || state.confirmPassword.isNotEmpty()) {
                    validateConfirmation(state.confirmPassword, password)
                } else null,
                status = state.nextStatus()
            )
        }
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.update { state ->
            state.copy(
                confirmPassword = password,
                confirmPasswordError = if (state.hasSubmitted || password.isNotEmpty()) {
                    validateConfirmation(password, state.password)
                } else null,
                status = state.nextStatus()
            )
        }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onToggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onTermsAcceptanceChange(accepted: Boolean) {
        _uiState.update { state ->
            state.copy(
                acceptedTerms = accepted,
                termsError = if (accepted) null else if (state.hasSubmitted) TERMS_REQUIRED_ERROR else null,
                status = state.nextStatus()
            )
        }
    }

    fun register() {
        val current = _uiState.value
        val errors = current.validated()
        if (errors.hasErrors()) {
            _uiState.update { it.copy(hasSubmitted = true).copyErrors(errors) }
            return
        }
        if (current.status == RegisterStatus.LOADING) return

        viewModelScope.launch {
            _uiState.update { it.copy(status = RegisterStatus.LOADING) }
            try {
                registerUser(current.name, current.email)
                _uiState.update { it.copy(status = RegisterStatus.SUCCESS) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update { it.copy(status = RegisterStatus.ERROR) }
            }
        }
    }

    fun onAcceptedTermsChange(accepted: Boolean) = onTermsAcceptanceChange(accepted)

    private fun RegisterUiState.nextStatus() =
        if (status == RegisterStatus.LOADING) RegisterStatus.LOADING else RegisterStatus.IDLE

    private fun RegisterUiState.validated() = ValidationErrors(
        name = validateName(name),
        email = validateEmail(email),
        password = validatePassword(password),
        confirmPassword = validateConfirmation(confirmPassword, password),
        terms = if (acceptedTerms) null else TERMS_REQUIRED_ERROR
    )

    private fun RegisterUiState.copyErrors(errors: ValidationErrors) = copy(
        nameError = errors.name,
        emailError = errors.email,
        passwordError = errors.password,
        confirmPasswordError = errors.confirmPassword,
        termsError = errors.terms
    )

    private fun ValidationErrors.hasErrors() =
        name != null || email != null || password != null || confirmPassword != null || terms != null

    private data class ValidationErrors(
        val name: String?,
        val email: String?,
        val password: String?,
        val confirmPassword: String?,
        val terms: String?
    )

    private fun validateName(name: String): String? =
        if (name.isBlank()) "El nombre es obligatorio." else null

    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> "El correo electrónico es obligatorio."
        !isValidEmail(email) -> "Ingresa un correo electrónico válido."
        else -> null
    }

    private fun validatePassword(password: String): String? = when {
        password.isBlank() -> "La contraseña es obligatoria."
        password.length < MIN_PASSWORD_LENGTH -> "La contraseña debe tener al menos 10 caracteres."
        password.none(Char::isUpperCase) -> "Agrega al menos una letra mayúscula."
        password.none(Char::isDigit) -> "Agrega al menos un número."
        else -> null
    }

    private fun validateConfirmation(confirmation: String, password: String): String? = when {
        confirmation.isBlank() -> "Confirma tu contraseña."
        confirmation != password -> "Las contraseñas no coinciden."
        else -> null
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 10
        const val TERMS_REQUIRED_ERROR = "Debes aceptar los términos y la política de tratamiento de datos."
    }
}
