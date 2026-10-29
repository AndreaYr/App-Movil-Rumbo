package com.app.rumbo.features.mypublications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.usecase.FinishPublicationUseCase
import com.app.rumbo.domain.usecase.GetMyPublicationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPublicationsViewModel @Inject constructor(
    getMyPublications: GetMyPublicationsUseCase,
    private val finishPublication: FinishPublicationUseCase
) : ViewModel() {

    private val selectedTab = MutableStateFlow(MyPublicationsTab.ALL)
    private val finishingId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MyPublicationsUiState> = combine(
        getMyPublications(),
        selectedTab,
        finishingId
    ) { publications, tab, finishing ->
        MyPublicationsUiState(
            publications = publications,
            selectedTab = tab,
            publicationToFinish = publications.firstOrNull { it.id == finishing }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyPublicationsUiState())

    fun onTabSelected(tab: MyPublicationsTab) {
        selectedTab.value = tab
    }

    /** Abre la confirmación para finalizar la publicación. */
    fun onFinishRequested(id: String) {
        finishingId.value = id
    }

    fun onFinishDismissed() {
        finishingId.value = null
    }

    fun onFinishConfirmed() {
        val id = finishingId.value ?: return
        finishingId.value = null
        viewModelScope.launch { finishPublication(id) }
    }
}
