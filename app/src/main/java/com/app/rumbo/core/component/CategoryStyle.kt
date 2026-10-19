package com.app.rumbo.core.component

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.app.rumbo.R
import com.app.rumbo.domain.model.Category

/** Color de cada categoría según el prototipo. */
val Category.color: Color
    get() = when (this) {
        Category.GASTRONOMY -> Color(0xFFE0742C)
        Category.CULTURE -> Color(0xFF7A5AC7)
        Category.NATURE -> Color(0xFF3E8E5A)
        Category.ENTERTAINMENT -> Color(0xFFD9407A)
        Category.HISTORY -> Color(0xFFB4841A)
    }

/** Icono de cada categoría para los selectores (crear publicación, filtros). */
val Category.iconRes: Int
    @DrawableRes
    get() = when (this) {
        Category.GASTRONOMY -> R.drawable.ic_category_gastronomy
        Category.CULTURE -> R.drawable.ic_category_culture
        Category.NATURE -> R.drawable.ic_category_nature
        Category.ENTERTAINMENT -> R.drawable.ic_star
        Category.HISTORY -> R.drawable.ic_category_history
    }
