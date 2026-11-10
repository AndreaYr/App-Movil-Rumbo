package com.app.rumbo.features.moderationfinished

import androidx.annotation.StringRes
import com.app.rumbo.R
import kotlinx.serialization.Serializable

/** Resultado de una acción del moderador; decide el texto y el estilo de la pantalla final. */
@Serializable
enum class ModerationOutcome(@param:StringRes val titleRes: Int, @param:StringRes val messageRes: Int) {
    APPROVED(R.string.outcome_approved_title, R.string.outcome_approved_message),
    REJECTED(R.string.outcome_rejected_title, R.string.outcome_rejected_message),
    FINISHED(R.string.outcome_finished_title, R.string.outcome_finished_message)
}
