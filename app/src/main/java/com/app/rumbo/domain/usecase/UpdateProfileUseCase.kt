package com.app.rumbo.domain.usecase

import com.app.rumbo.domain.model.ProfileField
import com.app.rumbo.domain.model.ProfileUpdate
import com.app.rumbo.domain.repository.UserRepository
import javax.inject.Inject

sealed interface UpdateProfileResult {
    data object Success : UpdateProfileResult
    data class Invalid(val fields: Set<ProfileField>) : UpdateProfileResult
}

/** Valida los datos del perfil y, solo si son correctos, los guarda ya sin espacios sobrantes. */
class UpdateProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(update: ProfileUpdate): UpdateProfileResult {
        val errors = update.errors
        if (errors.isNotEmpty()) return UpdateProfileResult.Invalid(errors)
        repository.updateProfile(
            ProfileUpdate(name = update.name.trim(), bio = update.bio.trim(), city = update.city.trim())
        )
        return UpdateProfileResult.Success
    }
}
