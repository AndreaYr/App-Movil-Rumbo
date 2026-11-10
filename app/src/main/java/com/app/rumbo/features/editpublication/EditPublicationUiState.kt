package com.app.rumbo.features.editpublication

import androidx.annotation.StringRes
import com.app.rumbo.R
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
    @get:StringRes
    val titleError: Int? get() = if (showValidationErrors && title.isBlank()) {
        R.string.create_title_required
    } else null

    @get:StringRes
    val descriptionError: Int? get() = if (showValidationErrors && description.isBlank()) {
        R.string.create_description_required
    } else null

    val isValid: Boolean get() = title.isNotBlank() && description.isNotBlank()
}
