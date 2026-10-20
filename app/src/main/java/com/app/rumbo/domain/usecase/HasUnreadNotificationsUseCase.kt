package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Indica si hay avisos sin leer; alimenta el punto de la campana en Inicio. */
class HasUnreadNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<Boolean> =
        repository.observeNotifications().map { list -> list.any { !it.isRead } }
}
