package com.app.rumbo.domain.model

import androidx.annotation.StringRes
import com.app.rumbo.R

enum class FeedSort(@param:StringRes val labelRes: Int) {
    RECENT(R.string.sort_recent),
    MOST_VOTED(R.string.sort_most_voted),
    NEAREST(R.string.sort_nearest)
}
