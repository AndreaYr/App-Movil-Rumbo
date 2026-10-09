package com.app.rumbo.core.component

import androidx.compose.ui.graphics.Color
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
