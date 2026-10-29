package com.app.rumbo.features.rejectpublication

import com.app.rumbo.domain.model.Publication

const val MAX_REJECTION_DETAIL_LENGTH = 300

data class RejectPublicationUiState(
    val publication: Publication? = null,
    val selectedReason: String? = null,
    val detail: String = ""
) {
    val charCount: Int get() = detail.length

    /** El motivo es obligatorio; el detalle es opcional. */
    val canConfirm: Boolean get() = selectedReason != null
}
