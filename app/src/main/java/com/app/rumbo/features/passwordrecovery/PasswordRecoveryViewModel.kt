package com.app.rumbo.features.passwordrecovery

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

enum class PasswordRecoveryStatus { IDLE, LOADING, SUCCESS, ERROR }

data class PasswordRecoveryUiState(
    val email: String = "",
    val emailError: String? = null,
    val status: PasswordRecoveryStatus = PasswordRecoveryStatus.IDLE,
    val message: String? = null
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
                message = null
            )
        }
    }

    fun sendLink() {
        val state = _uiState.value
        val error = validateEmail(state.email)
        if (error != null) {
            _uiState.update { it.copy(emailError = error, status = PasswordRecoveryStatus.IDLE, message = null) }
            return
        }
        if (state.status == PasswordRecoveryStatus.LOADING) return

        viewModelScope.launch {
            _uiState.update { it.copy(status = PasswordRecoveryStatus.LOADING, message = null) }
            delay(SIMULATED_REQUEST_DELAY_MS)
            _uiState.update {
                it.copy(
                    status = PasswordRecoveryStatus.SUCCESS,
                    message = SUCCESS_MESSAGE
                )
            }
        }
    }

    /** Activa un error visible de demostración, incluso si no se ingresó correo. */
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun showErrorDemo() {
        if (_uiState.value.status == PasswordRecoveryStatus.LOADING) return
        _uiState.update {
            it.copy(status = PasswordRecoveryStatus.ERROR, message = DEMO_ERROR_MESSAGE)
        }
    }

    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> "El correo electrónico es obligatorio."
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Ingresa un correo electrónico válido."
        else -> null
    }

    private companion object {
        const val SIMULATED_REQUEST_DELAY_MS = 800L
        const val SUCCESS_MESSAGE = "Solicitud simulada correctamente. No se envió ningún correo."
        const val DEMO_ERROR_MESSAGE = "Error de prueba: no se pudo completar la solicitud. Intenta de nuevo."
    }
}
