package com.app.rumbo.features.achievements

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

data class AchievementsUiState(
    val isLoading: Boolean = true,
    val unlocked: List<Badge> = emptyList(),
    val locked: List<Badge> = emptyList(),
    /** Puntos que suman los logros desbloqueados. */
    val pointsEarned: Int = 0
) {
    val total: Int get() = unlocked.size + locked.size

    /** 0f..1f de logros desbloqueados sobre el total. */
    val unlockedProgress: Float get() = if (total == 0) 0f else unlocked.size.toFloat() / total
}

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    getProfile: GetProfileUseCase
) : ViewModel() {

    val uiState: StateFlow<AchievementsUiState> = getProfile()
        .map { profile -> profile?.toUiState() ?: AchievementsUiState(isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AchievementsUiState())
}

private fun UserProfile.toUiState() = AchievementsUiState(
    isLoading = false,
    unlocked = badges.filter { it.isUnlocked },
    locked = badges.filterNot { it.isUnlocked },
    pointsEarned = level.points
)
