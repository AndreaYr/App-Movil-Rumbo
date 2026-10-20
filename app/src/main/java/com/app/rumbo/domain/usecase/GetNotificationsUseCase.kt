package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.NotificationsContent
import com.app.rumbo.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Agrupa los avisos en "Hoy" (últimas 24 h), "Esta semana" y "Antes". Con [unreadOnly]
 * solo se listan los no leídos, pero los contadores siempre cuentan todos.
 */
class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(unreadOnly: Boolean, now: () -> Long = System::currentTimeMillis): Flow<NotificationsContent> =
        repository.observeNotifications().map { all ->
            val visible = (if (unreadOnly) all.filterNot { it.isRead } else all).sortedByDescending { it.createdAt }
            val currentTime = now()
            val (today, rest) = visible.partition { currentTime - it.createdAt < DAY }
            val (thisWeek, earlier) = rest.partition { currentTime - it.createdAt < WEEK }
            NotificationsContent(
                today = today,
                thisWeek = thisWeek,
                earlier = earlier,
                totalCount = all.size,
                unreadCount = all.count { !it.isRead }
            )
        }

    private companion object {
        const val DAY = 24 * 60 * 60 * 1000L
        const val WEEK = 7 * DAY
    }
}
