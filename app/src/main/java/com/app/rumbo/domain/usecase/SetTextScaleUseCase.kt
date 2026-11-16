package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.TextScale
import com.app.rumbo.domain.repository.SettingsRepository
import javax.inject.Inject

/** Guarda el tamaño de letra elegido. */
class SetTextScaleUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(scale: TextScale) = repository.setTextScale(scale)
}
