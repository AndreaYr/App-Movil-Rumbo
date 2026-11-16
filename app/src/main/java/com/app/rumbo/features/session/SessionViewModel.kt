package com.app.rumbo.features.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.usecase.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Estado de la sesión que decide qué pantalla mostrar al abrir la aplicación. */
sealed interface SessionState {
    data object Loading : SessionState
    data object NotAuthenticated : SessionState
    data class Authenticated(val user: User) : SessionState
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    getCurrentUser: GetCurrentUserUseCase
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = getCurrentUser()
        .map { user ->
            if (user == null) SessionState.NotAuthenticated else SessionState.Authenticated(user)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)
}
