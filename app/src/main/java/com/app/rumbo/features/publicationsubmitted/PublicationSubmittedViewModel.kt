package com.app.rumbo.features.publicationsubmitted

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PublicationSubmittedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val title: String =
        checkNotNull(savedStateHandle[PublicationSubmittedDestination.ARG_TITLE]) { "Falta el título" }
}
