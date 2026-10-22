package com.app.rumbo.domain.model

const val MAX_NAME_LENGTH = 60
const val MAX_BIO_LENGTH = 200
const val MAX_CITY_LENGTH = 60

/** Datos que el usuario puede cambiar en "Editar perfil". El correo no se edita aquí. */
data class ProfileUpdate(
    val name: String,
    val bio: String,
    val city: String
) {
    /** Campos inválidos; vacío si se puede guardar. */
    val errors: Set<ProfileField>
        get() = buildSet {
            if (name.isBlank() || name.trim().length > MAX_NAME_LENGTH) add(ProfileField.NAME)
            if (bio.trim().length > MAX_BIO_LENGTH) add(ProfileField.BIO)
            if (city.isBlank() || city.trim().length > MAX_CITY_LENGTH) add(ProfileField.CITY)
        }

    val isValid: Boolean get() = errors.isEmpty()
}

enum class ProfileField { NAME, BIO, CITY }
