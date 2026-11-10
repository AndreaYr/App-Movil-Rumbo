package com.app.rumbo.features.loginmoderator

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.R
import com.app.rumbo.domain.usecase.LoginModeratorException
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
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    val isPasswordVisible: Boolean = false,
    val status: LoginModeratorStatus = LoginModeratorStatus.IDLE,
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
                messageRes = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { state ->
            state.copy(
                password = password,
                passwordError = if (password.isEmpty()) null else validatePassword(password),
                status = if (state.status == LoginModeratorStatus.LOADING) LoginModeratorStatus.LOADING else LoginModeratorStatus.IDLE,
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
        if (current.status == LoginModeratorStatus.LOADING) return
        val submittedEmail = current.email
        val submittedPassword = current.password

        viewModelScope.launch {
            _uiState.update { it.copy(status = LoginModeratorStatus.LOADING, messageRes = null) }
            loginModeratorUseCase(submittedEmail, submittedPassword)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            status = LoginModeratorStatus.SUCCESS,
                            messageRes = R.string.moderator_login_success_message
                        )
                    }
                }
                .onFailure { error ->
                    val messageRes = (error as? LoginModeratorException)?.messageRes
                        ?: R.string.moderator_login_error_generic
                    _uiState.update {
                        it.copy(
                            status = LoginModeratorStatus.ERROR,
                            messageRes = messageRes
                        )
                    }
                }
        }
    }

    fun resetResult() {
        _uiState.update { it.copy(status = LoginModeratorStatus.IDLE, messageRes = null) }
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
