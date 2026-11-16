package com.app.rumbo.domain.repository

import com.app.rumbo.domain.model.AppearanceSettings
import com.app.rumbo.domain.model.TextScale
import com.app.rumbo.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    /** Preferencias actuales; emite de nuevo cada vez que cambian. */
    fun observeAppearance(): Flow<AppearanceSettings>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setTextScale(scale: TextScale)
}
