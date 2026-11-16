package com.app.rumbo.core.component

import com.app.rumbo.core.theme.DarkGoldText
import com.app.rumbo.core.theme.adaptiveColor
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.app.rumbo.R
import com.app.rumbo.domain.model.Category

/** Color fijo de la categoría para los marcadores del mapa, que se dibujan fuera de Compose y no cambian con el tema. */
val Category.markerColor: Color
    get() = when (this) {
        Category.GASTRONOMY -> Color(0xFFE0742C)
        Category.CULTURE -> Color(0xFF7A5AC7)
        Category.NATURE -> Color(0xFF3E8E5A)
        Category.ENTERTAINMENT -> Color(0xFFD9407A)
        Category.HISTORY -> Color(0xFFB4841A)
    }

/** En el tema oscuro cada color es más claro para contrastar con el fondo. */
val Category.color: Color
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        Category.GASTRONOMY -> adaptiveColor(Color(0xFFE0742C), Color(0xFFFF9A5C))
        Category.CULTURE -> adaptiveColor(Color(0xFF7A5AC7), Color(0xFFB39DFF))
        Category.NATURE -> adaptiveColor(Color(0xFF3E8E5A), Color(0xFF6FCF97))
        Category.ENTERTAINMENT -> adaptiveColor(Color(0xFFD9407A), Color(0xFFFF7DA8))
        Category.HISTORY -> adaptiveColor(Color(0xFFB4841A), DarkGoldText)
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
