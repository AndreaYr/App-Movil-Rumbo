package com.app.rumbo.domain.model

/** Dónde está el usuario en la escalera de niveles, a partir de sus puntos. */
data class LevelProgress(val points: Int) {
    val level: Level = Level.forPoints(points)
    val nextLevel: Level? = level.next

    val pointsToNext: Int get() = nextLevel?.let { (it.minPoints - points).coerceAtLeast(0) } ?: 0

    /** 0f..1f dentro del nivel actual, para la barra de progreso. */
    val progress: Float
        get() {
            val next = nextLevel ?: return 1f
            return ((points - level.minPoints).toFloat() / (next.minPoints - level.minPoints)).coerceIn(0f, 1f)
        }
}
