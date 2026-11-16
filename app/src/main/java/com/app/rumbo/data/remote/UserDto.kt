package com.app.rumbo.data.remote

import androidx.annotation.Keep
import com.app.rumbo.domain.model.User
import com.app.rumbo.domain.model.UserRole

// Representación de un User tal como se guarda en Firestore.
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject().
// La contraseña no está aquí: la guarda y valida Firebase Authentication.
@Keep // Firestore llena esta clase buscando sus atributos por nombre
data class UserDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val city: String = "",
    val levelName: String = "",
    val bio: String = "",
    val role: String = UserRole.USER.name
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio
    fun toDomain(): User = User(
        id = id,
        name = name,
        email = email,
        city = city,
        levelName = levelName.ifBlank { "Explorador" },
        bio = bio,
        password = "", // La contraseña nunca se guarda en Firestore
        // Si llega un rol desconocido, se usa USER en lugar de romper la aplicación
        role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.USER)
    )

    companion object {
        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(user: User): UserDto = UserDto(
            id = user.id,
            name = user.name,
            email = user.email,
            city = user.city,
            levelName = user.levelName,
            bio = user.bio,
            role = user.role.name
        )
    }
}
