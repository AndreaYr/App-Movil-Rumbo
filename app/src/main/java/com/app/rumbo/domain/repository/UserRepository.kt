package com.app.rumbo.domain.repository

import com.app.rumbo.domain.model.ProfileUpdate
import com.app.rumbo.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /** Usuario actual compartido por la aplicación; null si no hay ninguno. */
    fun observeCurrentUser(): Flow<User?>

    /** Guarda al usuario recién creado y lo deja seleccionado como usuario actual. */
    suspend fun createUser(user: User): User

    /** Autentica al usuario; null si las credenciales son inválidas. */
    suspend fun login(email: String, password: String): User?

    /**
     * Inicia sesión (o registra, si es la primera vez) con una cuenta de Google.
     * @param idToken token de identidad entregado por Google.
     */
    suspend fun signInWithGoogle(idToken: String): User

    /**
     * Inicia sesión con Google solo si la cuenta ya está registrada en Rumbo; nunca crea el perfil.
     * @return el usuario, o null si esa cuenta de Google no está registrada.
     */
    suspend fun signInExistingWithGoogle(idToken: String): User?

    /** Envía al correo un enlace para restablecer la contraseña. */
    suspend fun sendPasswordReset(email: String)

    /** Cierra la sesión del usuario actual. */
    suspend fun logout()

    /** Guarda los cambios del perfil del usuario actual. */
    suspend fun updateProfile(update: ProfileUpdate)
}
