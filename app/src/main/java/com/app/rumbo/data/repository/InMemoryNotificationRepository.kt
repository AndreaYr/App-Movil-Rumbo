package com.app.rumbo.data.repository

import com.app.rumbo.data.local.SampleNotifications
import com.app.rumbo.domain.model.Notification
import com.app.rumbo.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** Repositorio en memoria: los cambios duran mientras la app esté abierta. */
@Singleton
class InMemoryNotificationRepository @Inject constructor() : NotificationRepository {

    private val notifications = MutableStateFlow(SampleNotifications.create())

    override fun observeNotifications(): Flow<List<Notification>> = notifications.asStateFlow()

    override suspend fun markAsRead(id: String) {
        notifications.update { list -> list.map { if (it.id == id) it.copy(isRead = true) else it } }
    }

    override suspend fun markAllAsRead() {
        notifications.update { list -> list.map { it.copy(isRead = true) } }
    }
}
