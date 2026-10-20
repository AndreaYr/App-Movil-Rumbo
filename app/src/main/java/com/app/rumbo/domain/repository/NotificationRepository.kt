package com.app.rumbo.domain.repository

import com.app.rumbo.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    /** Todos los avisos, del más reciente al más antiguo. */
    fun observeNotifications(): Flow<List<Notification>>

    suspend fun markAsRead(id: String)

    suspend fun markAllAsRead()
}
