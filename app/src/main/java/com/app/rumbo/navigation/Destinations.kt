package com.app.rumbo.navigation

import kotlinx.serialization.Serializable

// Rutas tipadas de Navigation Compose: cada pantalla es un objeto o clase serializable

@Serializable
data object FeedDestination

@Serializable
data object CreatePublicationDestination

@Serializable
data object LoginDestination

@Serializable
data object RegisterDestination

@Serializable
data object PasswordRecoveryDestination

@Serializable
data class PublicationDetailDestination(val publicationId: String) {
    companion object {
        /** Debe coincidir con el nombre de la propiedad: así la guarda Navigation en SavedStateHandle. */
        const val ARG_ID = "publicationId"
    }
}
