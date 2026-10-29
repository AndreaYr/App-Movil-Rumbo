package com.app.rumbo.features.editpublication

import com.app.rumbo.domain.model.Category
import com.app.rumbo.domain.model.Photo
import com.app.rumbo.domain.model.PriceLevel
import com.app.rumbo.domain.model.PublicationStatus

data class EditPublicationUiState(
    val isLoading: Boolean = true,
    val status: PublicationStatus = PublicationStatus.REJECTED,
    val createdAt: Long = 0L,
    val rejectionReason: String? = null,
    val rejectionDetail: String? = null,
    val photos: List<Photo> = emptyList(),
    val title: String = "",
    val category: Category = Category.NATURE,
    val description: String = "",
    val schedule: String = "",
    val price: PriceLevel = PriceLevel.CHEAP,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isSaving: Boolean = false,
    val showValidationErrors: Boolean = false
) {
    val titleError: String? get() = if (showValidationErrors && title.isBlank()) "Escribe un título" else null
    val descriptionError: String? get() = if (showValidationErrors && description.isBlank()) "Cuenta algo sobre el lugar" else null

    val isValid: Boolean get() = title.isNotBlank() && description.isNotBlank()
}
