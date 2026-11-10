package com.app.rumbo.domain.model

import androidx.annotation.StringRes
import com.app.rumbo.R

/** Escalera de niveles: cada nivel se alcanza al llegar a [minPoints], y los puntos vienen de los logros. */
enum class Level(val number: Int, @param:StringRes val displayNameRes: Int, val minPoints: Int) {
    TOURIST(1, R.string.level_tourist, 0),
    EXPLORER(2, R.string.level_explorer, 150),
    ADVENTURER(3, R.string.level_adventurer, 500),
    LOCAL_AMBASSADOR(4, R.string.level_local_ambassador, 1_000);

    /** Nivel siguiente; null si es el más alto. */
    val next: Level? get() = entries.getOrNull(ordinal + 1)

    companion object {
        fun forPoints(points: Int): Level = entries.last { points >= it.minPoints }
    }
}
