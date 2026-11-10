package com.app.rumbo.domain.model

import androidx.annotation.StringRes
import com.app.rumbo.R

enum class PriceLevel(@param:StringRes val labelRes: Int) {
    FREE(R.string.price_free),
    CHEAP(R.string.price_cheap),
    MODERATE(R.string.price_moderate),
    EXPENSIVE(R.string.price_expensive)
}
