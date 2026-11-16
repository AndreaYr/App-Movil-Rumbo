package com.app.rumbo.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.rumbo.domain.model.AppearanceSettings
import com.app.rumbo.domain.model.TextScale
import com.app.rumbo.domain.model.ThemeMode
import com.app.rumbo.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "rumbo_settings")

/** Guarda en el dispositivo (Preferences DataStore) el tema y el tamaño de letra. */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SettingsRepository {

    override fun observeAppearance(): Flow<AppearanceSettings> = context.settingsDataStore.data
        .catch { error ->
            // Si el archivo no se puede leer se usan los valores por defecto en lugar de cerrar la app
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw error
        }
        .map { preferences ->
            AppearanceSettings(
                // Un valor guardado que ya no existe (p. ej. tras una actualización) vuelve al valor por defecto
                themeMode = preferences[THEME_MODE]
                    ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
                    ?: ThemeMode.SYSTEM,
                textScale = preferences[TEXT_SCALE]
                    ?.let { name -> TextScale.entries.firstOrNull { it.name == name } }
                    ?: TextScale.NORMAL
            )
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[THEME_MODE] = mode.name }
    }

    override suspend fun setTextScale(scale: TextScale) {
        context.settingsDataStore.edit { it[TEXT_SCALE] = scale.name }
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val TEXT_SCALE = stringPreferencesKey("text_scale")
    }
}
