package com.app.rumbo.features.login

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.R
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
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    val isPasswordVisible: Boolean = false,
    val status: LoginStatus = LoginStatus.IDLE,
    @StringRes val messageRes: Int? = null
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
            _uiState.update { it.copy(status = LoginStatus.LOADING, messageRes = null) }
            delay(SIMULATED_REQUEST_DELAY_MS)
            val successful = submittedEmail.equals(TEST_EMAIL, ignoreCase = true) &&
                submittedPassword == TEST_PASSWORD
            _uiState.update {
                it.copy(
                    status = if (successful) LoginStatus.SUCCESS else LoginStatus.ERROR,
                    messageRes = if (successful) {
                        R.string.login_success_message
                    } else {
                        R.string.login_error_invalid_credentials
                    }
                )
            }
        }
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
        const val TEST_EMAIL = "prueba@rumbo.app"
        const val TEST_PASSWORD = "rumbo123"
        const val MIN_PASSWORD_LENGTH = 6
        const val SIMULATED_REQUEST_DELAY_MS = 800L
    }
}
