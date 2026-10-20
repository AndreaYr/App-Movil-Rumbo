package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkAllNotificationsReadUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke() = repository.markAllAsRead()
}
