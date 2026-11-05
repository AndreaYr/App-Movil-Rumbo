package com.app.rumbo.features.myroutes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.SavedRoute
import com.app.rumbo.domain.usecase.GetSavedRoutesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MyRoutesUiState(
    val isLoading: Boolean = true,
    val routes: List<SavedRoute> = emptyList()
)

@HiltViewModel
class MyRoutesViewModel @Inject constructor(
    getSavedRoutes: GetSavedRoutesUseCase
) : ViewModel() {
    val uiState: StateFlow<MyRoutesUiState> = getSavedRoutes()
        .map { MyRoutesUiState(isLoading = false, routes = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyRoutesUiState())
}
