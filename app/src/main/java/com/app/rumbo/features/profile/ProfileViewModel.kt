package com.app.rumbo.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.Badge
import com.app.rumbo.domain.model.UserProfile
import com.app.rumbo.domain.usecase.GetProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val initials: String = "",
    val levelNumber: Int = 1,
    val levelName: String = "",
    val points: Int = 0,
    /** Null cuando el usuario ya está en el nivel más alto. */
    val nextLevelName: String? = null,
    val nextLevelPoints: Int = 0,
    val pointsToNext: Int = 0,
    /** 0f..1f hacia el siguiente nivel. */
    val levelProgress: Float = 0f,
    val publicationsCount: Int = 0,
    val pendingPublicationsCount: Int = 0,
    val visitedCount: Int = 0,
    val routesCount: Int = 0,
    val unlockedBadges: List<Badge> = emptyList(),
    val lockedBadgesCount: Int = 0,
    val totalBadges: Int = 0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getProfile: GetProfileUseCase
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = getProfile()
        .map { profile -> profile?.toUiState() ?: ProfileUiState(isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())
}

private fun UserProfile.toUiState() = ProfileUiState(
    isLoading = false,
    name = user.name,
    initials = user.initials,
    levelNumber = level.level.number,
    levelName = level.level.displayName,
    points = level.points,
    nextLevelName = level.nextLevel?.displayName,
    nextLevelPoints = level.nextLevel?.minPoints ?: 0,
    pointsToNext = level.pointsToNext,
    levelProgress = level.progress,
    publicationsCount = publicationsCount,
    pendingPublicationsCount = pendingPublicationsCount,
    visitedCount = visitedCount,
    routesCount = routesCount,
    unlockedBadges = unlockedBadges,
    lockedBadgesCount = lockedBadgesCount,
    totalBadges = badges.size
)
