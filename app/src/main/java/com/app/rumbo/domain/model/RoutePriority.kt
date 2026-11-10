package com.app.rumbo.domain.model

import androidx.annotation.StringRes
import com.app.rumbo.R

/** Criterio con el que el planificador elige las paradas cuando no caben todas. */
enum class RoutePriority(@param:StringRes val labelRes: Int) {
    RELEVANT(R.string.priority_relevant),
    NEAREST(R.string.priority_nearest)
}
