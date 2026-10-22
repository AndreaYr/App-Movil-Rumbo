package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.ActivityStats
import com.app.rumbo.domain.model.Achievement
import com.app.rumbo.domain.model.Badge
import com.app.rumbo.domain.model.LevelProgress
import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.model.UserProfile
import com.app.rumbo.domain.repository.ActivityRepository
import com.app.rumbo.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Arma el perfil a partir de la actividad del usuario: cada logro se mide contra su meta,
 * los logros desbloqueados suman puntos y los puntos determinan el nivel.
 * Devuelve null si no hay un usuario con sesión iniciada.
 */
class GetProfileUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val activityRepository: ActivityRepository
) {
    operator fun invoke(): Flow<UserProfile?> =
        combine(userRepository.observeCurrentUser(), activityRepository.observeActivity()) { user, activity ->
            user?.let { buildProfile(it, activity) }
        }

    private fun buildProfile(user: User, activity: ActivityStats): UserProfile {
        val badges = Achievement.entries.map { Badge(it, current = it.metric(activity)) }
        val points = badges.filter { it.isUnlocked }.sumOf { it.achievement.points }
        return UserProfile(
            user = user,
            level = LevelProgress(points),
            publicationsCount = activity.publications,
            pendingPublicationsCount = activity.pendingPublications,
            visitedCount = activity.visitedPlaces,
            routesCount = activity.routesCreated,
            badges = badges
        )
    }
}
