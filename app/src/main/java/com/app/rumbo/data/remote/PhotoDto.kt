package com.app.rumbo.data.remote

import androidx.annotation.Keep
import com.app.rumbo.domain.model.Photo


@Keep // Firestore llena esta clase buscando sus atributos por nombre
data class PhotoDto(
    val url: String = "",
    val description: String = "",
    val credit: String? = null
) {
    fun toDomain(): Photo = Photo(
        url = url,
        description = description,
        credit = credit
    )

    companion object {
        fun fromDomain(photo: Photo): PhotoDto = PhotoDto(
            url = photo.url,
            description = photo.description,
            credit = photo.credit
        )
    }
}
