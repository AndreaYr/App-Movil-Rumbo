package com.app.rumbo.domain.model

/** Estado de los 14 logros para esta actividad. */
fun ActivityStats.toBadges(): List<Badge> =
    Achievement.entries.map { Badge(it, current = it.metric(this)) }

/** Puntos que suman los logros ya desbloqueados; de ellos sale el nivel. */
fun List<Badge>.earnedPoints(): Int = filter { it.isUnlocked }.sumOf { it.achievement.points }
