package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.ThemeMode
import com.app.rumbo.domain.repository.SettingsRepository
import javax.inject.Inject

/** Guarda el tema elegido (sistema, claro u oscuro). */
class SetThemeModeUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(mode: ThemeMode) = repository.setThemeMode(mode)
}
