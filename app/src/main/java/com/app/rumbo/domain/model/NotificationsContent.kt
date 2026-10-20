package com.app.rumbo.domain.model

/** Avisos agrupados por antigüedad, más los contadores de los filtros (no dependen del filtro activo). */
data class NotificationsContent(
    val today: List<Notification> = emptyList(),
    val thisWeek: List<Notification> = emptyList(),
    val earlier: List<Notification> = emptyList(),
    val totalCount: Int = 0,
    val unreadCount: Int = 0
) {
    val isEmpty: Boolean get() = today.isEmpty() && thisWeek.isEmpty() && earlier.isEmpty()
}
