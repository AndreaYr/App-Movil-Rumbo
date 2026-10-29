package com.app.rumbo.features.moderatorfinish

import com.app.rumbo.domain.model.Publication

data class ModeratorFinishUiState(
    val isLoading: Boolean = true,
    /** Publicación que se va a cerrar; `null` si no hay ninguna verificada. */
    val publication: Publication? = null,
    val isFinishing: Boolean = false
)
