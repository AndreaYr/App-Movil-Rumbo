package com.app.rumbo.domain.model

import androidx.annotation.StringRes
import com.app.rumbo.R

enum class PublicationStatus(@param:StringRes val labelRes: Int) {
    PENDING(R.string.status_pending),
    VERIFIED(R.string.status_verified),
    REJECTED(R.string.status_rejected),
    FINISHED(R.string.status_finished)
}
