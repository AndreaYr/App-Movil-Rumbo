package com.app.rumbo.features.publicationsubmitted

import kotlinx.serialization.Serializable

@Serializable
data class PublicationSubmittedDestination(val title: String) {
    companion object {
        const val ARG_TITLE = "title"
    }
}
