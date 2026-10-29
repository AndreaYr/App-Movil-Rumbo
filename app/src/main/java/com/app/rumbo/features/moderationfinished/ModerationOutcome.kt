package com.app.rumbo.features.moderationfinished

import kotlinx.serialization.Serializable

/** Resultado de una acción del moderador; decide el texto y el estilo de la pantalla final. */
@Serializable
enum class ModerationOutcome(val title: String, val message: String) {
    APPROVED("Publicación aprobada", "La publicación ya está visible en el feed público."),
    REJECTED("Publicación rechazada", "El autor verá el motivo del rechazo en Mis publicaciones."),
    FINISHED("Publicación finalizada", "Ya no aparece en el feed activo ni en el planificador de rutas.")
}
