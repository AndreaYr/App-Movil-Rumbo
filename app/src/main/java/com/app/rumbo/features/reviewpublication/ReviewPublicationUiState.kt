package com.app.rumbo.features.reviewpublication

import com.app.rumbo.domain.model.Publication

data class ReviewPublicationUiState(
    val publication: Publication? = null,
    val currentPhotoIndex: Int = 0
)
