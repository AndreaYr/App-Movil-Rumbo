package com.app.rumbo.features.rejectpublication

import androidx.annotation.StringRes
import com.app.rumbo.domain.model.Publication

const val MAX_REJECTION_DETAIL_LENGTH = 300

data class RejectPublicationUiState(
    val publication: Publication? = null,
    val selectedReason: String? = null,
    val detail: String = "",
    val isProcessing: Boolean = false,
    /** Mensaje de error por mostrar al moderador; la pantalla lo consume con `onErrorShown`. */
    @StringRes val errorRes: Int? = null
) {
    val charCount: Int get() = detail.length

    /** El motivo es obligatorio; el detalle es opcional. */
    val canConfirm: Boolean get() = selectedReason != null && !isProcessing
}
