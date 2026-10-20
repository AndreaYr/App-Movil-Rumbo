package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkNotificationReadUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(id: String) = repository.markAsRead(id)
}
