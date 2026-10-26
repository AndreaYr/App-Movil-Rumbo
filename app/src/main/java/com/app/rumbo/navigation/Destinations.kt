package com.app.rumbo.navigation

import kotlinx.serialization.Serializable

// Rutas tipadas de Navigation Compose: cada pantalla es un objeto o clase serializable

@Serializable
data object SplashDestination

@Serializable
data object LoginDestination

@Serializable
data object LoginModeratorDestination

@Serializable
data object ModeratorDashboardDestination

@Serializable
data object PendingPublicationsDestination

@Serializable
data class ReviewPublicationDestination(val publicationId: String)

@Serializable
data class RejectPublicationDestination(val publicationId: String)

@Serializable
data class ModerationFinishedDestination(
    val approved: Boolean,
    val rejectionReason: String? = null
)

@Serializable
data object ModeratorAccountDestination

@Serializable
data object RegisterDestination

@Serializable
data object PasswordRecoveryDestination

@Serializable
data object HomeDestination

@Serializable
data object FeedDestination

@Serializable
data object NotificationsDestination

@Serializable
data object ProfileDestination

@Serializable
data object EditProfileDestination

@Serializable
data object AchievementsDestination

@Serializable
data object CreatePublicationDestination

@Serializable
data class PublicationSubmittedDestination(val title: String) {
    companion object {
        const val ARG_TITLE = "title"
    }
}

@Serializable
data object MyPublicationsDestination

@Serializable
data class PublicationDetailDestination(val publicationId: String) {
    companion object {
        /** Debe coincidir con el nombre de la propiedad: así la guarda Navigation en SavedStateHandle. */
        const val ARG_ID = "publicationId"
    }
}
