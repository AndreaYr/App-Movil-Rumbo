package com.app.rumbo.domain.model

import androidx.annotation.StringRes
import com.app.rumbo.R

/**
 * Motivos que el moderador puede elegir al rechazar. En Firestore se guarda el nombre (por ejemplo "WRONG_LOCATION"),
 * no el texto, para que cada usuario lo lea en su idioma.
 */
enum class RejectionReason(@param:StringRes val labelRes: Int) {
    INSUFFICIENT_INFO(R.string.reject_reason_insufficient),
    WRONG_LOCATION(R.string.reject_reason_location),
    POOR_PHOTOS(R.string.reject_reason_photos),
    NOT_FOUND(R.string.reject_reason_not_found),
    INCONSISTENT(R.string.reject_reason_inconsistent);

    companion object {
        /** El motivo guardado con [code]; null si es un texto libre de una moderación anterior a este cambio. */
        fun fromCode(code: String?): RejectionReason? = entries.firstOrNull { it.name == code }
    }
}
