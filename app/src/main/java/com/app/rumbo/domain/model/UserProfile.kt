package com.app.rumbo.domain.model

/** Todo lo que necesita la pantalla de Perfil, ya calculado. */
data class UserProfile(
    val user: User,
    val level: LevelProgress,
    val publicationsCount: Int,
    val pendingPublicationsCount: Int,
    val visitedCount: Int,
    val routesCount: Int,
    val badges: List<Badge>
) {
    val unlockedBadges: List<Badge> get() = badges.filter { it.isUnlocked }
    val lockedBadgesCount: Int get() = badges.size - unlockedBadges.size
}
