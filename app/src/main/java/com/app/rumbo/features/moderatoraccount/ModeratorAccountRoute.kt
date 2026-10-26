package com.app.rumbo.features.moderatoraccount

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.rumbo.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

data class ModeratorAccountUiState(
    val name: String = "Moderador",
    val email: String = "moderador@rumbo.app",
    val role: String = "MODERADOR"
)

@HiltViewModel
class ModeratorAccountViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ModeratorAccountUiState())
    val uiState: StateFlow<ModeratorAccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                user?.let {
                    _uiState.update { state ->
                        state.copy(
                            name = it.name,
                            email = it.email
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModeratorAccountRoute(
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ModeratorAccountViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()
    ModeratorAccountScreen(state = state.value, onBack = onBack, onLogout = onLogout)
}
