package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.Notification
import com.app.rumbo.domain.model.NotificationType
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.repository.NotificationRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * Avisa al autor de una publicación que un moderador la verificó o la rechazó.
 * Es un complemento de la moderación: si el aviso no se puede guardar, la decisión del moderador se mantiene.
 */
class ModerationNotifier @Inject constructor(
    private val notifications: NotificationRepository
) {
    suspend fun notifyAuthor(publication: Publication?, type: NotificationType) {
        val authorId = publication?.author?.id?.takeIf { it.isNotBlank() && it != LOCAL_AUTHOR_ID } ?: return
        try {
            notifications.notifyUser(
                authorId,
                Notification(
                    id = "",
                    type = type,
                    text = "",
                    createdAt = System.currentTimeMillis(),
                    isRead = false,
                    publicationId = publication.id,
                    publicationTitle = publication.title
                )
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // Sin aviso, pero la publicación ya quedó moderada
        }
    }

    private companion object {
        /** Id que usa la app cuando no pudo leer el perfil del autor; no corresponde a ningún usuario. */
        const val LOCAL_AUTHOR_ID = "me"
    }
}
