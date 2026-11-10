package com.app.rumbo.features.passwordrecovery

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

enum class PasswordRecoveryStatus { IDLE, LOADING, SUCCESS, ERROR }

data class PasswordRecoveryUiState(
    val email: String = "",
    @StringRes val emailError: Int? = null,
    val status: PasswordRecoveryStatus = PasswordRecoveryStatus.IDLE,
    @StringRes val messageRes: Int? = null
)

@HiltViewModel
class PasswordRecoveryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordRecoveryUiState())
    val uiState: StateFlow<PasswordRecoveryUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { state ->
            state.copy(
                email = email,
                emailError = if (email.isBlank()) null else validateEmail(email),
                status = if (state.status == PasswordRecoveryStatus.LOADING) {
                    PasswordRecoveryStatus.LOADING
                } else {
                    PasswordRecoveryStatus.IDLE
                },
                messageRes = null
            )
        }
    }

    fun sendLink() {
        val state = _uiState.value
        val error = validateEmail(state.email)
        if (error != null) {
            _uiState.update { it.copy(emailError = error, status = PasswordRecoveryStatus.IDLE, messageRes = null) }
            return
        }
        if (state.status == PasswordRecoveryStatus.LOADING) return

        viewModelScope.launch {
            _uiState.update { it.copy(status = PasswordRecoveryStatus.LOADING, messageRes = null) }
            delay(SIMULATED_REQUEST_DELAY_MS)
            _uiState.update {
                it.copy(
                    status = PasswordRecoveryStatus.SUCCESS,
                    messageRes = R.string.password_recovery_success_message
                )
            }
        }
    }

    /** Activa un error visible de demostración, incluso si no se ingresó correo. */
    fun clearMessage() {
        _uiState.update { it.copy(messageRes = null) }
    }

    fun showErrorDemo() {
        if (_uiState.value.status == PasswordRecoveryStatus.LOADING) return
        _uiState.update {
            it.copy(
                status = PasswordRecoveryStatus.ERROR,
                messageRes = R.string.password_recovery_demo_error_message
            )
        }
    }

    @StringRes
    private fun validateEmail(email: String): Int? = when {
        email.isBlank() -> R.string.validation_email_required
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> R.string.validation_email_invalid
        else -> null
    }

    private companion object {
        const val SIMULATED_REQUEST_DELAY_MS = 800L
    }
}
