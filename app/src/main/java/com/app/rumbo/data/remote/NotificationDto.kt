package com.app.rumbo.data.remote

import androidx.annotation.Keep
import com.app.rumbo.domain.model.Notification
import com.app.rumbo.domain.model.NotificationType

// Representación de una Notification tal como se guarda en Firestore.
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject().
@Keep // Firestore llena esta clase buscando sus atributos por nombre
data class NotificationDto(
    val id: String = "",
    val type: String = NotificationType.RELEVANT.name,
    val text: String = "",
    val createdAt: Long = 0L,
    val isRead: Boolean = false,
    val publicationId: String? = null
) {
    fun toDomain(): Notification = Notification(
        id = id,
        type = runCatching { NotificationType.valueOf(type) }.getOrDefault(NotificationType.RELEVANT),
        text = text,
        createdAt = createdAt,
        isRead = isRead,
        publicationId = publicationId
    )

    companion object {
        fun fromDomain(notification: Notification): NotificationDto = NotificationDto(
            id = notification.id,
            type = notification.type.name,
            text = notification.text,
            createdAt = notification.createdAt,
            isRead = notification.isRead,
            publicationId = notification.publicationId
        )
    }
}
