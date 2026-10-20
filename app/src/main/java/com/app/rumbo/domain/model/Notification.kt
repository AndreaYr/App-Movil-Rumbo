package com.app.rumbo.domain.model

/** Aviso dirigido al usuario: moderación de sus publicaciones, actividad de otros y logros. */
data class Notification(
    val id: String,
    val type: NotificationType,
    val text: String,
    val createdAt: Long,
    val isRead: Boolean,
    /** Publicación a la que lleva el aviso; null si no apunta a ninguna pantalla disponible. */
    val publicationId: String? = null
)
