package com.app.rumbo.domain.model

/** Estado de un [Achievement] para un usuario: cuánto lleva y, si ya lo logró, cuándo. */
data class Badge(
    val achievement: Achievement,
    val current: Int,
    /** Fecha de desbloqueo. Es null hasta que exista persistencia para guardarla. */
    val unlockedAt: Long? = null
) {
    val isUnlocked: Boolean get() = current >= achievement.target

    /** 0f..1f para la barra de avance de un logro bloqueado. */
    val progress: Float get() = (current.toFloat() / achievement.target).coerceIn(0f, 1f)
}
