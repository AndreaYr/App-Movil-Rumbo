package com.app.rumbo.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.AiRecommendation
import com.app.rumbo.domain.model.HomeContent
import com.app.rumbo.domain.usecase.GetCurrentUserUseCase
import com.app.rumbo.domain.usecase.GetHomeUseCase
import com.app.rumbo.domain.usecase.HasUnreadNotificationsUseCase
import com.app.rumbo.domain.usecase.RecommendPlacesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val city: String = "",
    val hasUnreadNotifications: Boolean = false,
    val content: HomeContent = HomeContent(),
    /** Lugares "Para ti" elegidos por la IA según los gustos del usuario. */
    val recommendations: List<AiRecommendation> = emptyList(),
    val recommendationsLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getHome: GetHomeUseCase,
    getCurrentUser: GetCurrentUserUseCase,
    hasUnreadNotifications: HasUnreadNotificationsUseCase,
    private val recommendPlaces: RecommendPlacesUseCase
) : ViewModel() {

    private val recommendations = MutableStateFlow<List<AiRecommendation>?>(null)

    init {
        refreshRecommendations()
    }

    /** Pide de nuevo las recomendaciones; mientras responde la IA se muestra el estado de carga. */
    fun refreshRecommendations() {
        viewModelScope.launch {
            recommendations.value = null
            recommendations.value = recommendPlaces(AI_LANGUAGE)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(getHome(), getCurrentUser(), hasUnreadNotifications(), recommendations) { content, user, hasUnread, picks ->
        HomeUiState(
            isLoading = false,
            userName = user?.firstName.orEmpty(),
            city = user?.city.orEmpty(),
            hasUnreadNotifications = hasUnread,
            content = content,
            recommendations = picks.orEmpty(),
            recommendationsLoading = picks == null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}

/** Idioma de las frases de la IA: fijo en español mientras no se internacionalice. */
const val AI_LANGUAGE = "es"
