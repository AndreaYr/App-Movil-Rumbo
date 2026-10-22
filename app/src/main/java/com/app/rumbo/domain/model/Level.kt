package com.app.rumbo.domain.model

/** Escalera de niveles: cada nivel se alcanza al llegar a [minPoints], y los puntos vienen de los logros. */
enum class Level(val number: Int, val displayName: String, val minPoints: Int) {
    TOURIST(1, "Turista", 0),
    EXPLORER(2, "Exploradora", 150),
    ADVENTURER(3, "Aventurera", 500),
    LOCAL_AMBASSADOR(4, "Embajadora Local", 1_000);

    /** Nivel siguiente; null si es el más alto. */
    val next: Level? get() = entries.getOrNull(ordinal + 1)

    companion object {
        fun forPoints(points: Int): Level = entries.last { points >= it.minPoints }
    }
}
