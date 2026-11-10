package com.app.rumbo.domain.model

import androidx.annotation.StringRes
import com.app.rumbo.R

enum class Category(@param:StringRes val labelRes: Int) {
    GASTRONOMY(R.string.category_gastronomy),
    CULTURE(R.string.category_culture),
    NATURE(R.string.category_nature),
    ENTERTAINMENT(R.string.category_entertainment),
    HISTORY(R.string.category_history)
}
