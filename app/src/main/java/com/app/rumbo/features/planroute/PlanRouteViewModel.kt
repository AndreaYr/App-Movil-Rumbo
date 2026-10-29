package com.app.rumbo.features.planroute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.RoutePlan
import com.app.rumbo.domain.model.RoutePreferences
import com.app.rumbo.domain.model.RoutePriority
import com.app.rumbo.domain.model.Transport
import com.app.rumbo.domain.usecase.GetCurrentUserUseCase
import com.app.rumbo.domain.usecase.PlanRouteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PlanRouteUiState(
    val isLoading: Boolean = true,
    val city: String = "",
    val preferences: RoutePreferences = RoutePreferences(),
    val plan: RoutePlan = RoutePlan()
)

/** Cada cambio de parámetro recalcula la ruta al instante, como en el prototipo. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlanRouteViewModel @Inject constructor(
    planRoute: PlanRouteUseCase,
    getCurrentUser: GetCurrentUserUseCase
) : ViewModel() {

    private val preferences = MutableStateFlow(RoutePreferences())

    val uiState: StateFlow<PlanRouteUiState> = combine(
        preferences,
        preferences.flatMapLatest { planRoute(it) },
        getCurrentUser()
    ) { preferences, plan, user ->
        PlanRouteUiState(
            isLoading = false,
            city = user?.city.orEmpty(),
            preferences = preferences,
            plan = plan
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlanRouteUiState())

    fun onHoursChange(hours: Int) {
        preferences.update {
            it.copy(hours = hours.coerceIn(RoutePreferences.MIN_HOURS, RoutePreferences.MAX_HOURS))
        }
    }

    fun onBudgetChange(budget: PriceLevel) {
        preferences.update { it.copy(budget = budget) }
    }

    fun onCategoryToggle(category: Category) {
        preferences.update {
            it.copy(categories = if (category in it.categories) it.categories - category else it.categories + category)
        }
    }

    fun onTransportChange(transport: Transport) {
        preferences.update { it.copy(transport = transport) }
    }

    fun onPriorityChange(priority: RoutePriority) {
        preferences.update { it.copy(priority = priority) }
    }
}
