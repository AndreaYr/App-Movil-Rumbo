package com.app.rumbo.core.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

/** Resultado de pedirle a Google la identidad del usuario. */
sealed interface GoogleTokenResult {
    data class Success(val idToken: String) : GoogleTokenResult
    data object Cancelled : GoogleTokenResult
    data object Failure : GoogleTokenResult
}

/**
 * Muestra el selector de cuentas de Google (Credential Manager) y devuelve el ID token.
 * Necesita el contexto de la Activity para dibujar el diálogo, por eso lo invoca la UI
 * y entrega el token al ViewModel.
 */
object GoogleIdTokenProvider {

    suspend fun fetch(context: Context, webClientId: String): GoogleTokenResult {
        if (webClientId.isBlank()) return GoogleTokenResult.Failure
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false) // permite elegir cualquier cuenta, también para registrarse
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        return try {
            val credential = CredentialManager.create(context).getCredential(context, request).credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                GoogleTokenResult.Success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } else {
                GoogleTokenResult.Failure
            }
        } catch (_: GetCredentialCancellationException) {
            GoogleTokenResult.Cancelled
        } catch (_: GetCredentialException) {
            GoogleTokenResult.Failure
        } catch (_: GoogleIdTokenParsingException) {
            GoogleTokenResult.Failure
        }
    }
}
