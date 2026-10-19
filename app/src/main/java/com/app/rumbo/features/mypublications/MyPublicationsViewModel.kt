package com.app.rumbo.features.mypublications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.rumbo.domain.model.Publication
import com.app.rumbo.domain.usecase.GetMyPublicationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MyPublicationsViewModel @Inject constructor(
    getMyPublications: GetMyPublicationsUseCase
) : ViewModel() {
    val publications: StateFlow<List<Publication>> = getMyPublications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
