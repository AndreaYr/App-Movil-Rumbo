package com.app.rumbo.features.mypublications

import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus

/** Pestañas de "Mis publicaciones": todas o filtradas por estado. */
enum class MyPublicationsTab(val label: String, val status: PublicationStatus?) {
    ALL("Todas", null),
    PENDING("Pendientes", PublicationStatus.PENDING),
    VERIFIED("Verificadas", PublicationStatus.VERIFIED),
    REJECTED("Rechazadas", PublicationStatus.REJECTED),
    FINISHED("Finalizadas", PublicationStatus.FINISHED)
}

data class MyPublicationsUiState(
    val publications: List<Publication> = emptyList(),
    val selectedTab: MyPublicationsTab = MyPublicationsTab.ALL,
    /** Publicación que espera la confirmación para finalizarse; `null` si no hay ninguna. */
    val publicationToFinish: Publication? = null
) {
    val visiblePublications: List<Publication>
        get() = publications.filter { selectedTab.status == null || it.status == selectedTab.status }

    fun countFor(tab: MyPublicationsTab): Int =
        publications.count { tab.status == null || it.status == tab.status }
}
