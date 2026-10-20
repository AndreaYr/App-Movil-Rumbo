package com.app.rumbo.data.local

import com.app.rumbo.domain.model.Notification
import com.app.rumbo.domain.model.NotificationType

/** Avisos de ejemplo tomados del prototipo; se reemplazarán por la fuente real cuando exista. */
object SampleNotifications {

    private const val MINUTE = 60 * 1000L
    private const val HOUR = 60 * MINUTE
    private const val DAY = 24 * HOUR

    fun create(now: Long = System.currentTimeMillis()): List<Notification> = listOf(
        Notification(
            id = "n1",
            type = NotificationType.PUBLICATION_VERIFIED,
            text = "Tu publicación “Valle de Cocora” fue verificada y ya es pública.",
            createdAt = now - 25 * MINUTE,
            isRead = false,
            publicationId = "valle-cocora"
        ),
        Notification(
            id = "n2",
            type = NotificationType.COMMENT,
            text = "Julián Ramírez comentó en “Murales de Filandia”: “El mural nuevo de la calle 7 es increíble”.",
            createdAt = now - 2 * HOUR,
            isRead = false,
            publicationId = "murales-filandia"
        ),
        Notification(
            id = "n3",
            type = NotificationType.ACHIEVEMENT,
            text = "Nuevo logro desbloqueado: Creador de rutas · +50 puntos.",
            createdAt = now - 4 * HOUR,
            isRead = false
        ),
        Notification(
            id = "n4",
            type = NotificationType.PUBLICATION_REJECTED,
            text = "“Café de la Plaza” fue rechazada. Toca para ver el motivo y corregirla.",
            createdAt = now - 6 * HOUR,
            isRead = false
        ),
        Notification(
            id = "n5",
            type = NotificationType.TRENDING,
            text = "“Murales de Filandia” alcanzó 250 personas interesadas.",
            createdAt = now - 2 * DAY,
            isRead = true,
            publicationId = "murales-filandia"
        ),
        Notification(
            id = "n6",
            type = NotificationType.LEVEL_UP,
            text = "Subiste a Nivel 2 · Exploradora. Nuevas insignias disponibles.",
            createdAt = now - 4 * DAY,
            isRead = true
        ),
        Notification(
            id = "n7",
            type = NotificationType.RELEVANT,
            text = "Tu publicación “Valle de Cocora” fue marcada como relevante por 20 personas.",
            createdAt = now - 5 * DAY,
            isRead = true,
            publicationId = "valle-cocora"
        )
    )
}
