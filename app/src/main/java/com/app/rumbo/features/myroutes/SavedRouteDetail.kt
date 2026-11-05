package com.app.rumbo.features.myroutes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.SavedRoute
import com.app.rumbo.domain.usecase.GetSavedRoutesUseCase
import com.app.rumbo.features.planroute.RouteScreen
import com.app.rumbo.navigation.SavedRouteDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SavedRouteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getSavedRoutes: GetSavedRoutesUseCase
) : ViewModel() {
    private val routeId: String =
        checkNotNull(savedStateHandle[SavedRouteDestination.ARG_ROUTE_ID]) { "Falta la ruta" }

    val route: StateFlow<SavedRoute?> = getSavedRoutes()
        .map { routes -> routes.firstOrNull { it.id == routeId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
fun SavedRouteRoute(
    onBack: () -> Unit,
    onStopClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavedRouteViewModel = hiltViewModel()
) {
    val route by viewModel.route.collectAsStateWithLifecycle()
    val current = route
    if (current == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    RouteScreen(
        route = current,
        isSaved = true,
        onBack = onBack,
        onSave = {},
        onStopClick = onStopClick,
        modifier = modifier
    )
}
