package com.app.rumbo.domain.model

/** Persona que usa la app. Sus datos llegan del registro y del inicio de sesión. */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val city: String,
    val levelName: String,
    /** Presentación breve del usuario ("Sobre mí"); puede estar vacía. */
    val bio: String = "",
    /**
     * Contraseña usada solo para crear la cuenta en Firebase Authentication.
     * Nunca se guarda en Firestore: el DTO la deja vacía al leer un documento.
     */
    val password: String = "",
    val role: UserRole = UserRole.USER
) {
    /** "Mariana Ospina" -> "Mariana" */
    val firstName: String
        get() = name.trim().substringBefore(' ')

    /** "Mariana Ospina" -> "MO" */
    val initials: String
        get() = name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }
}
