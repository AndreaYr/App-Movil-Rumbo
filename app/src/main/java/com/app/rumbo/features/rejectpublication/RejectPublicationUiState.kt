package com.app.rumbo.features.rejectpublication

import com.app.rumbo.domain.model.Publication

data class RejectPublicationUiState(
    val publication: Publication? = null,
    val selectedReason: String? = "Información inconsistente",
    val detail: String = "El marcador quedó sobre la vía principal, no sobre el mirador. Corrige la ubicación y vuelve a enviarla.",
    val charCount: Int = 112,
    val isProcessing: Boolean = false,
    val actionFailed: Boolean = false,
    val moderationCompleted: Boolean = false,
    val completedReason: String? = null
)
