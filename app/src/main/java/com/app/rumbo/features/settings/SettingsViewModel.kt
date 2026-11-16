package com.app.rumbo.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.AppearanceSettings
import com.app.rumbo.domain.model.TextScale
import com.app.rumbo.domain.model.ThemeMode
import com.app.rumbo.domain.usecase.ObserveAppearanceUseCase
import com.app.rumbo.domain.usecase.SetTextScaleUseCase
import com.app.rumbo.domain.usecase.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Preferencias de apariencia. Lo usan la Activity (para aplicar el tema y el tamaño de letra a toda la app)
 * y la hoja de configuración (para cambiarlos); ambos leen el mismo repositorio, por eso se ven al instante.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeAppearance: ObserveAppearanceUseCase,
    private val setThemeMode: SetThemeModeUseCase,
    private val setTextScale: SetTextScaleUseCase
) : ViewModel() {

    val appearance: StateFlow<AppearanceSettings> = observeAppearance()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppearanceSettings())

    fun onThemeModeSelected(mode: ThemeMode) {
        viewModelScope.launch { setThemeMode(mode) }
    }

    fun onTextScaleSelected(scale: TextScale) {
        viewModelScope.launch { setTextScale(scale) }
    }
}
