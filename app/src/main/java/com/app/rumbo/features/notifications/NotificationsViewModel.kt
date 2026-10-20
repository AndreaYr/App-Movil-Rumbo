package com.app.rumbo.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.NotificationsContent
import com.app.rumbo.domain.usecase.GetNotificationsUseCase
import com.app.rumbo.domain.usecase.MarkAllNotificationsReadUseCase
import com.app.rumbo.domain.usecase.MarkNotificationReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NotificationFilter { ALL, UNREAD }

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val filter: NotificationFilter = NotificationFilter.ALL,
    val content: NotificationsContent = NotificationsContent()
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotifications: GetNotificationsUseCase,
    private val markNotificationRead: MarkNotificationReadUseCase,
    private val markAllNotificationsRead: MarkAllNotificationsReadUseCase
) : ViewModel() {

    private val filter = MutableStateFlow(NotificationFilter.ALL)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<NotificationsUiState> = filter
        .flatMapLatest { selected ->
            getNotifications(unreadOnly = selected == NotificationFilter.UNREAD).map { content ->
                NotificationsUiState(isLoading = false, filter = selected, content = content)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationsUiState())

    fun onFilterSelected(selected: NotificationFilter) {
        filter.value = selected
    }

    fun onNotificationOpened(id: String) {
        viewModelScope.launch { markNotificationRead(id) }
    }

    fun onMarkAllRead() {
        viewModelScope.launch { markAllNotificationsRead() }
    }
}
