package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.AppearanceSettings
import com.app.rumbo.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Entrega el tema y el tamaño de letra que eligió el usuario. */
class ObserveAppearanceUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<AppearanceSettings> = repository.observeAppearance()
}
