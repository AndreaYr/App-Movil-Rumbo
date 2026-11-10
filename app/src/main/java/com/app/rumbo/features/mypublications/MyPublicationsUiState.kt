package com.app.rumbo.features.mypublications

import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.model.PublicationStatus
import androidx.annotation.StringRes
import com.app.rumbo.R

/** Pestañas de "Mis publicaciones": todas o filtradas por estado. */
enum class MyPublicationsTab(@param:StringRes val labelRes: Int, val status: PublicationStatus?) {
    ALL(R.string.my_publications_tab_all, null),
    PENDING(R.string.my_publications_tab_pending, PublicationStatus.PENDING),
    VERIFIED(R.string.my_publications_tab_verified, PublicationStatus.VERIFIED),
    REJECTED(R.string.my_publications_tab_rejected, PublicationStatus.REJECTED),
    FINISHED(R.string.my_publications_tab_finished, PublicationStatus.FINISHED)
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
