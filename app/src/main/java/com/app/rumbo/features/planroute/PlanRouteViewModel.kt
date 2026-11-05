package com.app.rumbo.features.planroute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.RoutePlan
import com.app.rumbo.domain.model.RoutePreferences
import com.app.rumbo.domain.model.RoutePriority
import com.app.rumbo.domain.model.Transport
import com.app.rumbo.domain.model.toSavedRoute
import com.app.rumbo.domain.usecase.GetCurrentUserUseCase
import com.app.rumbo.domain.usecase.GetSavedRoutesUseCase
import com.app.rumbo.domain.usecase.PlanRouteUseCase
import com.app.rumbo.domain.usecase.SaveRouteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class PlanRouteUiState(
    val isLoading: Boolean = true,
    val city: String = "",
    val preferences: RoutePreferences = RoutePreferences(),
    val plan: RoutePlan = RoutePlan(),
    /** La ruta actual ya está en "Mis rutas". */
    val isRouteSaved: Boolean = false
)

/** Cada cambio de parámetro recalcula la ruta al instante, como en el prototipo. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlanRouteViewModel @Inject constructor(
    planRoute: PlanRouteUseCase,
    getCurrentUser: GetCurrentUserUseCase,
    getSavedRoutes: GetSavedRoutesUseCase,
    private val saveRoute: SaveRouteUseCase
) : ViewModel() {

    private val preferences = MutableStateFlow(RoutePreferences())

    val uiState: StateFlow<PlanRouteUiState> = combine(
        preferences,
        preferences.flatMapLatest { planRoute(it) },
        getCurrentUser(),
        getSavedRoutes()
    ) { preferences, plan, user, savedRoutes ->
        val route = plan.toSavedRoute(preferences)
        PlanRouteUiState(
            isLoading = false,
            city = user?.city.orEmpty(),
            preferences = preferences,
            plan = plan,
            isRouteSaved = savedRoutes.any { it.isSameRouteAs(route) }
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

    /** Guarda la ruta actual; [onResult] indica si se pudo escribir. */
    fun onSaveRoute(onResult: (Boolean) -> Unit) {
        val state = uiState.value
        viewModelScope.launch {
            val saved = try {
                saveRoute(state.plan, state.preferences)
                true
            } catch (_: IOException) {
                false
            }
            onResult(saved)
        }
    }
}
