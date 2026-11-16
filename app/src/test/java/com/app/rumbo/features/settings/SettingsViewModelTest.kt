package com.app.rumbo.features.settings

import com.app.rumbo.domain.model.AppearanceSettings
import com.app.rumbo.domain.model.TextScale
import com.app.rumbo.domain.model.ThemeMode
import com.app.rumbo.domain.repository.SettingsRepository
import com.app.rumbo.domain.usecase.ObserveAppearanceUseCase
import com.app.rumbo.domain.usecase.SetTextScaleUseCase
import com.app.rumbo.domain.usecase.SetThemeModeUseCase
import com.app.rumbo.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeSettingsRepository : SettingsRepository {
        private val state = MutableStateFlow(AppearanceSettings())
        override fun observeAppearance(): Flow<AppearanceSettings> = state.asStateFlow()
        override suspend fun setThemeMode(mode: ThemeMode) = state.update { it.copy(themeMode = mode) }
        override suspend fun setTextScale(scale: TextScale) = state.update { it.copy(textScale = scale) }
    }

    private fun viewModel(): SettingsViewModel {
        val repository = FakeSettingsRepository()
        return SettingsViewModel(
            ObserveAppearanceUseCase(repository),
            SetThemeModeUseCase(repository),
            SetTextScaleUseCase(repository)
        )
    }

    @Test
    fun `por defecto sigue al sistema y usa letra normal`() {
        val settings = viewModel().appearance.value

        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertEquals(TextScale.NORMAL, settings.textScale)
    }

    @Test
    fun `al elegir tema y tamano de letra el estado se actualiza`() {
        val viewModel = viewModel()

        viewModel.onThemeModeSelected(ThemeMode.DARK)
        viewModel.onTextScaleSelected(TextScale.EXTRA_LARGE)

        assertEquals(ThemeMode.DARK, viewModel.appearance.value.themeMode)
        assertEquals(TextScale.EXTRA_LARGE, viewModel.appearance.value.textScale)
    }
}
