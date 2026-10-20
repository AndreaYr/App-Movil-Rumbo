package com.app.rumbo.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.HomeContent
import com.app.rumbo.domain.usecase.GetCurrentUserUseCase
import com.app.rumbo.domain.usecase.GetHomeUseCase
import com.app.rumbo.domain.usecase.HasUnreadNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val city: String = "",
    val hasUnreadNotifications: Boolean = false,
    val content: HomeContent = HomeContent()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getHome: GetHomeUseCase,
    getCurrentUser: GetCurrentUserUseCase,
    hasUnreadNotifications: HasUnreadNotificationsUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(getHome(), getCurrentUser(), hasUnreadNotifications()) { content, user, hasUnread ->
        HomeUiState(
            isLoading = false,
            userName = user?.firstName.orEmpty(),
            city = user?.city.orEmpty(),
            hasUnreadNotifications = hasUnread,
            content = content
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
