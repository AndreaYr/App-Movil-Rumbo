package com.app.rumbo.core.component

import androidx.annotation.DrawableRes
import com.app.rumbo.R
import com.app.rumbo.domain.model.Achievement

/** Icono de cada logro; el dominio no conoce recursos de Android. */
val Achievement.iconRes: Int
    @DrawableRes
    get() = when (this) {
        Achievement.FIRST_PUBLICATION -> R.drawable.ic_flag
        Achievement.TEN_VERIFIED, Achievement.LOCAL_GUIDE -> R.drawable.ic_verified
        Achievement.FIRST_VISIT, Achievement.WALKER, Achievement.CITY_EXPLORER -> R.drawable.ic_check_circle
        Achievement.TEN_COMMENTS, Achievement.COMMUNITY_VOICE -> R.drawable.ic_chat_bubble
        Achievement.FIFTY_VOTES, Achievement.COMMUNITY_FAVORITE -> R.drawable.ic_thumb_up
        Achievement.ROUTE_CREATOR, Achievement.ROUTE_MASTER, Achievement.ROUTE_COMPLETED -> R.drawable.ic_route
        Achievement.MONTH_HIGHLIGHT -> R.drawable.ic_star
    }
