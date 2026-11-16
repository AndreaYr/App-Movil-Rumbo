package com.app.rumbo.features.reviewpublication

import androidx.annotation.StringRes
import com.app.rumbo.domain.model.Publication

data class ReviewPublicationUiState(
    val publication: Publication? = null,
    val currentPhotoIndex: Int = 0,
    val isProcessing: Boolean = false,
    /** Mensaje de error por mostrar al moderador; la pantalla lo consume con `onErrorShown`. */
    @StringRes val errorRes: Int? = null
)
