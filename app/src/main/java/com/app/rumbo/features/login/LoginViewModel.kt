package com.app.rumbo.features.login

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.R
import com.app.rumbo.domain.exception.InvalidRoleException
import com.app.rumbo.domain.usecase.LoginUserUseCase
import com.app.rumbo.domain.usecase.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LoginStatus { IDLE, LOADING, SUCCESS, ERROR }

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    val isPasswordVisible: Boolean = false,
    val status: LoginStatus = LoginStatus.IDLE,
    @StringRes val messageRes: Int? = null,
    /** El inicio de sesión en curso es el de Google; el botón de correo no muestra la rueda de carga. */
    val isGoogleLoading: Boolean = false
) {
    val isFormValid: Boolean
        get() = emailError == null && passwordError == null &&
            Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= MIN_PASSWORD_LENGTH

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { state ->
            state.copy(
                email = email,
                emailError = if (email.isBlank()) null else validateEmail(email),
                status = if (state.status == LoginStatus.LOADING) LoginStatus.LOADING else LoginStatus.IDLE,
                messageRes = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { state ->
            state.copy(
                password = password,
                passwordError = if (password.isEmpty()) null else validatePassword(password),
                status = if (state.status == LoginStatus.LOADING) LoginStatus.LOADING else LoginStatus.IDLE,
                messageRes = null
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
            _uiState.update { it.copy(status = LoginStatus.LOADING, isGoogleLoading = false, messageRes = null) }

            try {
                val user = loginUserUseCase(submittedEmail, submittedPassword)
                _uiState.update {
                    it.copy(
                        status = if (user != null) LoginStatus.SUCCESS else LoginStatus.ERROR,
                        messageRes = if (user != null) {
                            R.string.login_success_message
                        } else {
                            R.string.login_error_invalid_credentials
                        }
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: InvalidRoleException) {
                // Un moderador no puede entrar por el acceso de usuarios
                showError(R.string.auth_error_invalid_credentials_or_role)
            } catch (_: Exception) {
                showError(R.string.login_error_generic)
            }
        }
    }

    /** Recibe el ID token de Google y abre sesión (o crea la cuenta si es la primera vez). */
    fun loginWithGoogle(idToken: String) {
        if (_uiState.value.status == LoginStatus.LOADING) return
        viewModelScope.launch {
            _uiState.update { it.copy(status = LoginStatus.LOADING, isGoogleLoading = true, messageRes = null) }
            try {
                signInWithGoogleUseCase(idToken)
                _uiState.update {
                    it.copy(status = LoginStatus.SUCCESS, isGoogleLoading = false, messageRes = R.string.login_success_message)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: InvalidRoleException) {
                showError(R.string.auth_error_invalid_credentials_or_role)
            } catch (_: Exception) {
                onGoogleError()
            }
        }
    }

    /** Google no entregó credenciales (sin cuentas, sin red o configuración incompleta). */
    fun onGoogleError() = showError(R.string.google_sign_in_error)

    private fun showError(@StringRes messageRes: Int) {
        _uiState.update { it.copy(status = LoginStatus.ERROR, isGoogleLoading = false, messageRes = messageRes) }
    }

    fun resetResult() {
        _uiState.update { it.copy(status = LoginStatus.IDLE, messageRes = null) }
    }

    @StringRes
    private fun validateEmail(email: String): Int? = when {
        email.isBlank() -> R.string.validation_email_required
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> R.string.validation_email_invalid
        else -> null
    }

    @StringRes
    private fun validatePassword(password: String): Int? = when {
        password.isBlank() -> R.string.validation_password_required
        password.length < MIN_PASSWORD_LENGTH -> R.string.validation_password_min_length_6
        else -> null
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
