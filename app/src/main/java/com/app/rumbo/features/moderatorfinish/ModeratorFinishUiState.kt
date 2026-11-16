package com.app.rumbo.features.moderatorfinish

import androidx.annotation.StringRes
import com.app.rumbo.domain.model.Publication

data class ModeratorFinishUiState(
    val isLoading: Boolean = true,
    /** Publicaciones verificadas que se pueden cerrar, las más antiguas primero. */
    val publications: List<Publication> = emptyList(),
    /** Id de la publicación que el moderador eligió cerrar; si no eligió ninguna se usa la primera. */
    val selectedId: String? = null,
    val isFinishing: Boolean = false,
    /** Mensaje de error por mostrar al moderador; la pantalla lo consume con `onErrorShown`. */
    @StringRes val errorRes: Int? = null
) {
    /** Publicación que se va a cerrar; `null` si no hay ninguna verificada. */
    val publication: Publication?
        get() = publications.firstOrNull { it.id == selectedId } ?: publications.firstOrNull()
}
